package com.example.consumptionplan.module.category.controller;

import com.example.consumptionplan.common.Result;
import com.example.consumptionplan.module.category.entity.Category;
import com.example.consumptionplan.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public Result<List<Category>> listCategories(@RequestParam(required = false) Integer type) {
        return Result.success(categoryService.listByType(type));
    }
}
