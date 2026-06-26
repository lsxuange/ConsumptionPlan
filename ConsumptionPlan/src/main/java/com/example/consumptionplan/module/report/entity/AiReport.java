package com.example.consumptionplan.module.report.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_report")
public class AiReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String reportMonth;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal budgetAmount;

    private String reportContent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
