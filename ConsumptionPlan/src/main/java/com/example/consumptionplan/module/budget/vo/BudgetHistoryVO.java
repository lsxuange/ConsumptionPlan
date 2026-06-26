package com.example.consumptionplan.module.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetHistoryVO {
    private String budgetMonth;
    private BigDecimal budgetAmount;
    private BigDecimal totalExpense;
    private BigDecimal usageRate;
}
