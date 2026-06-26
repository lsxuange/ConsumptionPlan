package com.example.consumptionplan.module.record.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.consumptionplan.common.Result;
import com.example.consumptionplan.module.record.dto.RecordDTO;
import com.example.consumptionplan.module.record.dto.RecordQueryDTO;
import com.example.consumptionplan.module.record.service.RecordService;
import com.example.consumptionplan.module.record.vo.RecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @PostMapping("/records")
    public Result<Void> addRecord(@RequestBody @Valid RecordDTO dto) {
        Long userId = getCurrentUserId();
        recordService.addRecord(userId, dto);
        return Result.success();
    }

    @PutMapping("/records/{id}")
    public Result<Void> updateRecord(@PathVariable Long id, @RequestBody @Valid RecordDTO dto) {
        Long userId = getCurrentUserId();
        recordService.updateRecord(userId, id, dto);
        return Result.success();
    }

    @DeleteMapping("/records/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        recordService.deleteRecord(userId, id);
        return Result.success();
    }

    @GetMapping("/records")
    public Result<IPage<RecordVO>> pageRecords(RecordQueryDTO query) {
        Long userId = getCurrentUserId();
        return Result.success(recordService.pageRecords(userId, query));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
