package com.example.consumptionplan.module.budget.controller;

import com.example.consumptionplan.common.Result;
import com.example.consumptionplan.module.budget.dto.BudgetDTO;
import com.example.consumptionplan.module.budget.service.BudgetService;
import com.example.consumptionplan.module.budget.vo.BudgetHistoryVO;
import com.example.consumptionplan.module.budget.vo.BudgetVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping("/budget")
    public Result<Void> saveOrUpdateBudget(@RequestBody @Valid BudgetDTO dto) {
        Long userId = getCurrentUserId();
        budgetService.saveOrUpdateBudget(userId, dto);
        return Result.success();
    }

    @GetMapping("/budget")
    public Result<BudgetVO> getBudgetVO(@RequestParam(required = false) String month) {
        if (month == null || month.isBlank()) {
            month = YearMonth.now().toString();
        }
        Long userId = getCurrentUserId();
        return Result.success(budgetService.getBudgetVO(userId, month));
    }

    @GetMapping("/budget/history")
    public Result<List<BudgetHistoryVO>> getHistory() {
        Long userId = getCurrentUserId();
        return Result.success(budgetService.getHistory(userId));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
