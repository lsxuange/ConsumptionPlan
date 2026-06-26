package com.example.consumptionplan.module.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.consumptionplan.module.category.entity.Category;
import com.example.consumptionplan.module.category.mapper.CategoryMapper;
import com.example.consumptionplan.module.record.dto.RecordDTO;
import com.example.consumptionplan.module.record.dto.RecordQueryDTO;
import com.example.consumptionplan.module.record.entity.ExpenseRecord;
import com.example.consumptionplan.module.record.mapper.ExpenseRecordMapper;
import com.example.consumptionplan.module.record.service.RecordService;
import com.example.consumptionplan.module.record.vo.RecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final ExpenseRecordMapper expenseRecordMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public void addRecord(Long userId, RecordDTO dto) {
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        ExpenseRecord record = new ExpenseRecord();
        record.setUserId(userId);
        record.setCategoryId(dto.getCategoryId());
        record.setType(dto.getType());
        record.setAmount(dto.getAmount());
        record.setRemark(dto.getRemark());
        record.setRecordDate(dto.getRecordDate());
        expenseRecordMapper.insert(record);
    }

    @Override
    public void updateRecord(Long userId, Long recordId, RecordDTO dto) {
        ExpenseRecord record = expenseRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        record.setCategoryId(dto.getCategoryId());
        record.setType(dto.getType());
        record.setAmount(dto.getAmount());
        record.setRemark(dto.getRemark());
        record.setRecordDate(dto.getRecordDate());
        expenseRecordMapper.updateById(record);
    }

    @Override
    public void deleteRecord(Long userId, Long recordId) {
        ExpenseRecord record = expenseRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        expenseRecordMapper.deleteById(recordId);
    }

    @Override
    public IPage<RecordVO> pageRecords(Long userId, RecordQueryDTO query) {
        Page<ExpenseRecord> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<ExpenseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpenseRecord::getUserId, userId);
        if (query.getType() != null) {
            wrapper.eq(ExpenseRecord::getType, query.getType());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(ExpenseRecord::getCategoryId, query.getCategoryId());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(ExpenseRecord::getRecordDate, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(ExpenseRecord::getRecordDate, query.getEndDate());
        }
        wrapper.orderByDesc(ExpenseRecord::getRecordDate)
               .orderByDesc(ExpenseRecord::getCreateTime);
        IPage<ExpenseRecord> result = expenseRecordMapper.selectPage(page, wrapper);
        return result.convert(this::toVO);
    }

    private RecordVO toVO(ExpenseRecord record) {
        RecordVO vo = new RecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setCategoryId(record.getCategoryId());
        vo.setType(record.getType());
        vo.setAmount(record.getAmount());
        vo.setRemark(record.getRemark());
        vo.setRecordDate(record.getRecordDate());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());
        Category category = categoryMapper.selectById(record.getCategoryId());
        vo.setCategoryName(category != null ? category.getName() : null);
        return vo;
    }
}
