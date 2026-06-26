package com.example.consumptionplan.module.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("check_in_record")
public class CheckInRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate checkDate;

    private LocalDateTime createTime;
}
