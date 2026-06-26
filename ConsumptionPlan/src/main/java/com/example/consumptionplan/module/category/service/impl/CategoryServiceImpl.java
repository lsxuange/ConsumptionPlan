package com.example.consumptionplan.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.consumptionplan.module.category.entity.Category;
import com.example.consumptionplan.module.category.mapper.CategoryMapper;
import com.example.consumptionplan.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> listByType(Integer type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Category::getType, type);
        }
        wrapper.orderByAsc(Category::getSortNum);
        return categoryMapper.selectList(wrapper);
    }
}
