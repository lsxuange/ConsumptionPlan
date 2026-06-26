package com.example.consumptionplan.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TrendVO {
    private String month;
    private BigDecimal income;
    private BigDecimal expense;
}
