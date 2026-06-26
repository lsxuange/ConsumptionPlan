package com.example.consumptionplan.module.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardVO {
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private BigDecimal monthBalance;
    private BigDecimal budgetAmount;
    private BigDecimal budgetUsageRate;
    private List<CategoryStatVO> expenseCategoryList;
    private List<TrendVO> trendList;
    private List<CategoryStatVO> incomeSourceList;
}
