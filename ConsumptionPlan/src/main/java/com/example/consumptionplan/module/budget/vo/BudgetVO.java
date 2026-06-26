package com.example.consumptionplan.module.budget.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetVO {
    private String budgetMonth;
    private BigDecimal amount;
    private BigDecimal totalExpense;
    private BigDecimal remaining;
    private BigDecimal usageRate;
}
