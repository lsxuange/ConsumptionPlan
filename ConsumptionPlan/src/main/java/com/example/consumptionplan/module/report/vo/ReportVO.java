package com.example.consumptionplan.module.report.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReportVO {
    private String reportMonth;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal budgetAmount;
    private String reportContent;
    private LocalDateTime createTime;
}
