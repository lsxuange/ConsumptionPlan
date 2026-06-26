package com.example.consumptionplan.module.category.service;

import com.example.consumptionplan.module.category.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> listByType(Integer type);
}
