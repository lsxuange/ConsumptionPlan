package com.example.consumptionplan.module.budget.service;

import com.example.consumptionplan.module.budget.dto.BudgetDTO;
import com.example.consumptionplan.module.budget.vo.BudgetHistoryVO;
import com.example.consumptionplan.module.budget.vo.BudgetVO;

import java.util.List;

public interface BudgetService {

    void saveOrUpdateBudget(Long userId, BudgetDTO dto);

    BudgetVO getBudgetVO(Long userId, String budgetMonth);

    List<BudgetHistoryVO> getHistory(Long userId);
}
