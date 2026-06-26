package com.example.consumptionplan.module.checkin.controller;

import com.example.consumptionplan.common.Result;
import com.example.consumptionplan.module.checkin.service.CheckInService;
import com.example.consumptionplan.module.checkin.vo.CheckInVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/checkin")
    public Result<Void> checkIn() {
        Long userId = getCurrentUserId();
        checkInService.checkIn(userId);
        return Result.success();
    }

    @GetMapping("/checkin/status")
    public Result<CheckInVO> getCheckInStatus() {
        Long userId = getCurrentUserId();
        return Result.success(checkInService.getCheckInVO(userId));
    }

    @GetMapping("/checkin/month")
    public Result<List<String>> getCheckedDates(@RequestParam(required = false) String month) {
        Long userId = getCurrentUserId();
        String targetMonth = (month == null || month.isEmpty()) ? YearMonth.now().toString() : month;
        return Result.success(checkInService.listCheckedDates(userId, targetMonth));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}