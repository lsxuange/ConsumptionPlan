package com.example.consumptionplan.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.consumptionplan.config.DeepSeekConfig;
import com.example.consumptionplan.module.budget.entity.Budget;
import com.example.consumptionplan.module.budget.mapper.BudgetMapper;
import com.example.consumptionplan.module.category.entity.Category;
import com.example.consumptionplan.module.category.mapper.CategoryMapper;
import com.example.consumptionplan.module.notification.service.NotificationService;
import com.example.consumptionplan.module.record.entity.ExpenseRecord;
import com.example.consumptionplan.module.record.mapper.ExpenseRecordMapper;
import com.example.consumptionplan.module.report.entity.AiReport;
import com.example.consumptionplan.module.report.mapper.AiReportMapper;
import com.example.consumptionplan.module.report.service.ReportService;
import com.example.consumptionplan.module.report.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AiReportMapper aiReportMapper;
    private final ExpenseRecordMapper expenseRecordMapper;
    private final BudgetMapper budgetMapper;
    private final CategoryMapper categoryMapper;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;
    private final DeepSeekConfig deepSeekConfig;

    @Override
    public ReportVO generateReport(Long userId, String reportMonth) {
        YearMonth ym = YearMonth.parse(reportMonth);
        LocalDate firstDay = ym.atDay(1);
        LocalDate nextMonthFirst = ym.plusMonths(1).atDay(1);

        BigDecimal totalIncome = sumAmount(userId, 1, firstDay, nextMonthFirst);
        BigDecimal totalExpense = sumAmount(userId, 0, firstDay, nextMonthFirst);

        Budget budget = budgetMapper.selectOne(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getBudgetMonth, reportMonth));
        BigDecimal budgetAmount = budget != null ? budget.getAmount() : BigDecimal.ZERO;

        String categoryStats = buildCategoryStats(userId, firstDay, nextMonthFirst);

        String prompt = "你是一个个人财务分析师。请根据以下数据生成消费分析报告：\n"
                + "月份：" + reportMonth + "\n"
                + "总收入：" + totalIncome + " 元\n"
                + "总支出：" + totalExpense + " 元\n"
                + "月预算：" + budgetAmount + " 元\n"
                + "支出分类明细：" + categoryStats + "\n"
                + "请输出：1.消费行为分析 2.消费习惯评价 3.财务健康程度 4.优化建议 5.下月预算建议";

        String reportContent = callDeepSeek(prompt);

        AiReport existing = aiReportMapper.selectOne(new LambdaQueryWrapper<AiReport>()
                .eq(AiReport::getUserId, userId)
                .eq(AiReport::getReportMonth, reportMonth));
        if (existing != null) {
            existing.setTotalIncome(totalIncome);
            existing.setTotalExpense(totalExpense);
            existing.setBudgetAmount(budgetAmount);
            existing.setReportContent(reportContent);
            aiReportMapper.updateById(existing);
        } else {
            AiReport report = new AiReport();
            report.setUserId(userId);
            report.setReportMonth(reportMonth);
            report.setTotalIncome(totalIncome);
            report.setTotalExpense(totalExpense);
            report.setBudgetAmount(budgetAmount);
            report.setReportContent(reportContent);
            aiReportMapper.insert(report);
        }

        notificationService.send(userId, "AI报告已生成", reportMonth + " 消费分析报告已生成");

        return toVO(reportMonth, totalIncome, totalExpense, budgetAmount, reportContent);
    }

    @Override
    public ReportVO getReport(Long userId, String reportMonth) {
        AiReport report = aiReportMapper.selectOne(new LambdaQueryWrapper<AiReport>()
                .eq(AiReport::getUserId, userId)
                .eq(AiReport::getReportMonth, reportMonth));
        if (report == null) {
            throw new RuntimeException("暂无报告，请先生成");
        }
        ReportVO vo = new ReportVO();
        vo.setReportMonth(report.getReportMonth());
        vo.setTotalIncome(report.getTotalIncome());
        vo.setTotalExpense(report.getTotalExpense());
        vo.setBudgetAmount(report.getBudgetAmount());
        vo.setReportContent(report.getReportContent());
        vo.setCreateTime(report.getCreateTime());
        return vo;
    }

    @Override
    public List<ReportVO> listReports(Long userId) {
        List<AiReport> reports = aiReportMapper.selectList(
                new LambdaQueryWrapper<AiReport>()
                        .eq(AiReport::getUserId, userId)
                        .orderByDesc(AiReport::getCreateTime));
        return reports.stream().map(r -> {
            ReportVO vo = new ReportVO();
            vo.setReportMonth(r.getReportMonth());
            vo.setTotalIncome(r.getTotalIncome());
            vo.setTotalExpense(r.getTotalExpense());
            vo.setBudgetAmount(r.getBudgetAmount());
            vo.setReportContent(r.getReportContent());
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
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

    private String buildCategoryStats(Long userId, LocalDate start, LocalDate end) {
        List<ExpenseRecord> records = expenseRecordMapper.selectList(
                new LambdaQueryWrapper<ExpenseRecord>()
                        .eq(ExpenseRecord::getUserId, userId)
                        .eq(ExpenseRecord::getType, 0)
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
                    return name + ":" + e.getValue() + "元";
                })
                .collect(Collectors.joining(", "));
    }

    @SuppressWarnings("unchecked")
    private String callDeepSeek(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekConfig.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-chat");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("max_tokens", 1000);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    deepSeekConfig.getApiUrl(), entity, Map.class);
            List<Map> choices = (List<Map>) response.getBody().get("choices");
            Map message = (Map) ((Map) choices.get(0)).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("AI报告生成失败，请稍后重试");
        }
    }

    private ReportVO toVO(String reportMonth, BigDecimal totalIncome, BigDecimal totalExpense,
                          BigDecimal budgetAmount, String reportContent) {
        ReportVO vo = new ReportVO();
        vo.setReportMonth(reportMonth);
        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setBudgetAmount(budgetAmount);
        vo.setReportContent(reportContent);
        return vo;
    }
}
