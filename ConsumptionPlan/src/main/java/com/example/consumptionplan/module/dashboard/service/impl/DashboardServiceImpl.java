package com.example.consumptionplan.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.consumptionplan.module.budget.entity.Budget;
import com.example.consumptionplan.module.budget.mapper.BudgetMapper;
import com.example.consumptionplan.module.category.entity.Category;
import com.example.consumptionplan.module.category.mapper.CategoryMapper;
import com.example.consumptionplan.module.dashboard.service.DashboardService;
import com.example.consumptionplan.module.dashboard.vo.CategoryStatVO;
import com.example.consumptionplan.module.dashboard.vo.DashboardVO;
import com.example.consumptionplan.module.dashboard.vo.TrendVO;
import com.example.consumptionplan.module.record.entity.ExpenseRecord;
import com.example.consumptionplan.module.record.mapper.ExpenseRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRecordMapper expenseRecordMapper;
    private final BudgetMapper budgetMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public DashboardVO getDashboard(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate firstDay = currentMonth.atDay(1);
        LocalDate nextMonthFirst = currentMonth.plusMonths(1).atDay(1);

        BigDecimal monthIncome = sumAmount(userId, 1, firstDay, nextMonthFirst);
        BigDecimal monthExpense = sumAmount(userId, 0, firstDay, nextMonthFirst);
        BigDecimal monthBalance = monthIncome.subtract(monthExpense);

        Budget budget = budgetMapper.selectOne(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getBudgetMonth, currentMonth.toString()));
        BigDecimal budgetAmount = budget != null ? budget.getAmount() : BigDecimal.ZERO;

        BigDecimal budgetUsageRate = budgetAmount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : monthExpense.multiply(new BigDecimal("100")).divide(budgetAmount, 2, RoundingMode.HALF_UP);

        List<CategoryStatVO> expenseCategoryList = buildCategoryStats(userId, 0, firstDay, nextMonthFirst, monthExpense);
        List<CategoryStatVO> incomeSourceList = buildCategoryStats(userId, 1, firstDay, nextMonthFirst, monthIncome);
        List<TrendVO> trendList = buildTrendList(userId);

        DashboardVO vo = new DashboardVO();
        vo.setMonthIncome(monthIncome);
        vo.setMonthExpense(monthExpense);
        vo.setMonthBalance(monthBalance);
        vo.setBudgetAmount(budgetAmount);
        vo.setBudgetUsageRate(budgetUsageRate);
        vo.setExpenseCategoryList(expenseCategoryList);
        vo.setTrendList(trendList);
        vo.setIncomeSourceList(incomeSourceList);
        return vo;
    }

    private BigDecimal sumAmount(Long userId, int type, LocalDate start, LocalDate end) {
        List<ExpenseRecord> records = expenseRecordMapper.selectList(
                new LambdaQueryWrapper<ExpenseRecord>()
                        .eq(ExpenseRecord::getUserId, userId)
                        .eq(ExpenseRecord::getType, type)
                        .ge(ExpenseRecord::getRecordDate, start)
                        .lt(ExpenseRecord::getRecordDate, end));
        return records.stream()
                .map(ExpenseRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategoryStatVO> buildCategoryStats(Long userId, int type,
                                                     LocalDate start, LocalDate end,
                                                     BigDecimal total) {
        List<ExpenseRecord> records = expenseRecordMapper.selectList(
                new LambdaQueryWrapper<ExpenseRecord>()
                        .eq(ExpenseRecord::getUserId, userId)
                        .eq(ExpenseRecord::getType, type)
                        .ge(ExpenseRecord::getRecordDate, start)
                        .lt(ExpenseRecord::getRecordDate, end));
        Map<Long, BigDecimal> grouped = records.stream()
                .collect(Collectors.groupingBy(
                        ExpenseRecord::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, ExpenseRecord::getAmount, BigDecimal::add)));
        return grouped.entrySet().stream()
                .map(e -> {
                    Category cat = categoryMapper.selectById(e.getKey());
                    String name = cat != null ? cat.getName() : "未知";
                    BigDecimal amount = e.getValue();
                    BigDecimal percentage = total.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : amount.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP);
                    return new CategoryStatVO(name, amount, percentage);
                })
                .sorted(Comparator.comparing(CategoryStatVO::getAmount).reversed())
                .collect(Collectors.toList());
    }

    private List<TrendVO> buildTrendList(Long userId) {
        List<TrendVO> trendList = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDate firstDay = ym.atDay(1);
            LocalDate nextMonthFirst = ym.plusMonths(1).atDay(1);
            BigDecimal income = sumAmount(userId, 1, firstDay, nextMonthFirst);
            BigDecimal expense = sumAmount(userId, 0, firstDay, nextMonthFirst);
            trendList.add(new TrendVO(ym.toString(), income, expense));
        }
        return trendList;
    }
}
