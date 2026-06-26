package com.example.consumptionplan.module.record.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RecordQueryDTO {
    private Integer type;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 1;
    private Integer size = 10;
}
