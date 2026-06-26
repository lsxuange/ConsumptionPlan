package com.example.consumptionplan.module.dashboard.controller;

import com.example.consumptionplan.common.Result;
import com.example.consumptionplan.module.dashboard.service.DashboardService;
import com.example.consumptionplan.module.dashboard.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        Long userId = getCurrentUserId();
        return Result.success(dashboardService.getDashboard(userId));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
