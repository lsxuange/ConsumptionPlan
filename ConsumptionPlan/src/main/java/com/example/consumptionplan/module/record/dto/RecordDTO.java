package com.example.consumptionplan.module.record.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecordDTO {

    @NotNull
    private Long categoryId;

    @NotNull
    private Integer type;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private String remark;

    @NotNull
    private LocalDate recordDate;
}
