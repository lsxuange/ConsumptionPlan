package com.example.consumptionplan.module.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackDTO {

    @NotBlank
    @Size(max = 1000)
    private String content;
}
