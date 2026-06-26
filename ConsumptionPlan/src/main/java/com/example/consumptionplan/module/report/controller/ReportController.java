package com.example.consumptionplan.module.report.controller;

import com.example.consumptionplan.common.Result;
import com.example.consumptionplan.module.report.service.ReportService;
import com.example.consumptionplan.module.report.vo.ReportVO;
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
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/report/generate")
    public Result<ReportVO> generateReport(@RequestParam(required = false) String month) {
        if (month == null || month.isBlank()) {
            month = YearMonth.now().toString();
        }
        Long userId = getCurrentUserId();
        return Result.success(reportService.generateReport(userId, month));
    }

    @GetMapping("/report")
    public Result<ReportVO> getReport(@RequestParam(required = false) String month) {
        if (month == null || month.isBlank()) {
            month = YearMonth.now().toString();
        }
        Long userId = getCurrentUserId();
        return Result.success(reportService.getReport(userId, month));
    }

    @GetMapping("/reports")
    public Result<List<ReportVO>> listReports() {
        Long userId = getCurrentUserId();
        return Result.success(reportService.listReports(userId));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
