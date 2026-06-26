package com.example.consumptionplan.module.user.controller;

import com.example.consumptionplan.common.Result;
import com.example.consumptionplan.module.user.dto.ChangePasswordDTO;
import com.example.consumptionplan.module.user.dto.LoginDTO;
import com.example.consumptionplan.module.user.dto.RegisterDTO;
import com.example.consumptionplan.module.user.dto.UpdateUserDTO;
import com.example.consumptionplan.module.user.service.UserService;
import com.example.consumptionplan.module.user.vo.LoginVO;
import com.example.consumptionplan.module.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/auth/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/user/info")
    public Result<UserVO> getUserInfo() {
        Long userId = getCurrentUserId();
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/user/info")
    public Result<Void> updateUserInfo(@RequestBody @Valid UpdateUserDTO dto) {
        Long userId = getCurrentUserId();
        userService.updateUserInfo(userId, dto);
        return Result.success();
    }

    @PutMapping("/user/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changePassword(userId, dto);
        return Result.success(null);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
