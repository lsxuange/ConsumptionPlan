package com.example.consumptionplan.module.checkin.vo;

import lombok.Data;

@Data
public class CheckInVO {
    private Boolean todayChecked;
    private Integer continuousDays;
    private Integer monthCheckedDays;
}