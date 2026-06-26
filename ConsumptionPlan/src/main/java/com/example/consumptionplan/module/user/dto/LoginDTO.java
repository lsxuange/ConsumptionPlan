package com.example.consumptionplan.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
