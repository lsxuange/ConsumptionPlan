package com.example.consumptionplan.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryStatVO {
    private String categoryName;
    private BigDecimal amount;
    private BigDecimal percentage;
}
