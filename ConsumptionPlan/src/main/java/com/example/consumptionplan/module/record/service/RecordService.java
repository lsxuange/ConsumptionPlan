package com.example.consumptionplan.module.record.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.consumptionplan.module.record.dto.RecordDTO;
import com.example.consumptionplan.module.record.dto.RecordQueryDTO;
import com.example.consumptionplan.module.record.vo.RecordVO;

public interface RecordService {

    void addRecord(Long userId, RecordDTO dto);

    void updateRecord(Long userId, Long recordId, RecordDTO dto);

    void deleteRecord(Long userId, Long recordId);

    IPage<RecordVO> pageRecords(Long userId, RecordQueryDTO query);
}
