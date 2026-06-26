package com.example.consumptionplan.module.budget.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.consumptionplan.module.budget.dto.BudgetDTO;
import com.example.consumptionplan.module.budget.entity.Budget;
import com.example.consumptionplan.module.budget.mapper.BudgetMapper;
import com.example.consumptionplan.module.budget.service.BudgetService;
import com.example.consumptionplan.module.budget.vo.BudgetHistoryVO;
import com.example.consumptionplan.module.budget.vo.BudgetVO;
import com.example.consumptionplan.module.notification.service.NotificationService;
import com.example.consumptionplan.module.record.entity.ExpenseRecord;
import com.example.consumptionplan.module.record.mapper.ExpenseRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetMapper budgetMapper;
    private final ExpenseRecordMapper expenseRecordMapper;
    private final NotificationService notificationService;

    @Override
    public void saveOrUpdateBudget(Long userId, BudgetDTO dto) {
        Budget existing = budgetMapper.selectOne(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getBudgetMonth, dto.getBudgetMonth()));
        if (existing != null) {
            existing.setAmount(dto.getAmount());
            budgetMapper.updateById(existing);
        } else {
            Budget budget = new Budget();
            budget.setUserId(userId);
            budget.setBudgetMonth(dto.getBudgetMonth());
            budget.setAmount(dto.getAmount());
            budgetMapper.insert(budget);
        }
        BigDecimal totalExpense = calcMonthExpense(userId, dto.getBudgetMonth());
        BigDecimal amount = dto.getAmount();
        BigDecimal usageRate = BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            usageRate = totalExpense.divide(amount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        if (usageRate.compareTo(BigDecimal.valueOf(100)) >= 0) {
            notificationService.send(userId, "⚠️ 预算已超支",
                    String.format("本月（%s）预算 ¥%.2f，当前支出已达 ¥%.2f，已超出预算！请立即控制消费。",
                            dto.getBudgetMonth(), amount, totalExpense));
        } else if (usageRate.compareTo(BigDecimal.valueOf(80)) >= 0) {
            notificationService.send(userId, "⚠️ 预算接近超支",
                    String.format("本月（%s）预算使用率已达 %.2f%%，剩余 ¥%.2f，请注意消费节奏。",
                            dto.getBudgetMonth(), usageRate, amount.subtract(totalExpense)));
        } else if (usageRate.compareTo(BigDecimal.valueOf(60)) >= 0) {
            notificationService.send(userId, "💡 预算使用提醒",
                    String.format("本月（%s）预算已使用 %.2f%%，还剩 ¥%.2f，继续保持！",
                            dto.getBudgetMonth(), usageRate, amount.subtract(totalExpense)));
        }
    }

    @Override
    public BudgetVO getBudgetVO(Long userId, String budgetMonth) {
        Budget budget = budgetMapper.selectOne(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getBudgetMonth, budgetMonth));
        BigDecimal amount = budget != null ? budget.getAmount() : BigDecimal.ZERO;
        BigDecimal totalExpense = calcMonthExpense(userId, budgetMonth);
        BigDecimal remaining = amount.subtract(totalExpense);
        BigDecimal usageRate = amount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalExpense.multiply(new BigDecimal("100")).divide(amount, 2, RoundingMode.HALF_UP);

        BudgetVO vo = new BudgetVO();
        vo.setBudgetMonth(budgetMonth);
        vo.setAmount(amount);
        vo.setTotalExpense(totalExpense);
        vo.setRemaining(remaining);
        vo.setUsageRate(usageRate);
        return vo;
    }

    @Override
    public List<BudgetHistoryVO> getHistory(Long userId) {
        // 1. 生成最近6个月（含当月），从旧到新
        YearMonth now = YearMonth.now();
        List<String> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            months.add(now.minusMonths(i).toString());
        }

        // 2. 批量查预算
        List<Budget> budgets = budgetMapper.selectList(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .in(Budget::getBudgetMonth, months));
        Map<String, Budget> budgetMap = budgets.stream()
                .collect(Collectors.toMap(Budget::getBudgetMonth, b -> b));

        // 3-4. 逐月组装
        List<BudgetHistoryVO> result = new ArrayList<>();
        for (String month : months) {
            Budget budget = budgetMap.get(month);
            BigDecimal budgetAmount = budget != null ? budget.getAmount() : null;
            BigDecimal totalExpense = calcMonthExpense(userId, month);

            BigDecimal usageRate = null;
            if (budgetAmount != null) {
                usageRate = totalExpense.multiply(new BigDecimal("100"))
                        .divide(budgetAmount, 2, RoundingMode.HALF_UP);
            }

            result.add(new BudgetHistoryVO(month, budgetAmount, totalExpense, usageRate));
        }
        return result;
    }

    private BigDecimal calcMonthExpense(Long userId, String budgetMonth) {
        YearMonth ym = YearMonth.parse(budgetMonth);
        LocalDate firstDay = ym.atDay(1);
        LocalDate nextMonthFirst = ym.plusMonths(1).atDay(1);
        List<ExpenseRecord> records = expenseRecordMapper.selectList(
                new LambdaQueryWrapper<ExpenseRecord>()
                        .eq(ExpenseRecord::getUserId, userId)
                        .eq(ExpenseRecord::getType, 0)
                        .ge(ExpenseRecord::getRecordDate, firstDay)
                        .lt(ExpenseRecord::getRecordDate, nextMonthFirst));
        return records.stream()
                .map(ExpenseRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
