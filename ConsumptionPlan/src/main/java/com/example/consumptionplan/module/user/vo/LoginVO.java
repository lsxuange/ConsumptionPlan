package com.example.consumptionplan.module.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {
    private String token;
    private String username;
    private String avatar;
}
