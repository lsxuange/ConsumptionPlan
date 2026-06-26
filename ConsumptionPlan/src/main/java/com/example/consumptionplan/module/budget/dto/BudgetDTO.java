package com.example.consumptionplan.module.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetDTO {

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}")
    private String budgetMonth;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
