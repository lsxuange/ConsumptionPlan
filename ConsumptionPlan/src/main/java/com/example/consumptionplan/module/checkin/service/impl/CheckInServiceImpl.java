package com.example.consumptionplan.module.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.consumptionplan.module.checkin.entity.CheckInRecord;
import com.example.consumptionplan.module.checkin.mapper.CheckInRecordMapper;
import com.example.consumptionplan.module.checkin.service.CheckInService;
import com.example.consumptionplan.module.checkin.vo.CheckInVO;
import com.example.consumptionplan.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRecordMapper checkInRecordMapper;
    private final NotificationService notificationService;

    @Override
    public void checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        Long count = checkInRecordMapper.selectCount(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getUserId, userId)
                .eq(CheckInRecord::getCheckDate, today));
        if (count > 0) {
            throw new RuntimeException("今日已打卡");
        }
        CheckInRecord record = new CheckInRecord();
        record.setUserId(userId);
        record.setCheckDate(today);
        checkInRecordMapper.insert(record);
        notificationService.send(userId, "打卡成功", "今日记账打卡成功，继续保持！");
    }

    @Override
    public CheckInVO getCheckInVO(Long userId) {
        LocalDate today = LocalDate.now();

        Boolean todayChecked = checkInRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckInRecord>()
                        .eq(CheckInRecord::getUserId, userId)
                        .eq(CheckInRecord::getCheckDate, today)) > 0;

        Integer monthCheckedDays = Math.toIntExact(checkInRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckInRecord>()
                        .eq(CheckInRecord::getUserId, userId)
                        .ge(CheckInRecord::getCheckDate, today.withDayOfMonth(1))));

        Integer continuousDays = calcContinuousDays(userId, today);

        CheckInVO vo = new CheckInVO();
        vo.setTodayChecked(todayChecked);
        vo.setMonthCheckedDays(monthCheckedDays);
        vo.setContinuousDays(continuousDays);
        return vo;
    }

    @Override
    public List<String> listCheckedDates(Long userId, String month) {
        YearMonth ym = (month == null || month.isEmpty()) ? YearMonth.now() : YearMonth.parse(month);
        LocalDate firstDay = ym.atDay(1);
        LocalDate lastDay = ym.atEndOfMonth();

        List<CheckInRecord> records = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .eq(CheckInRecord::getUserId, userId)
                        .ge(CheckInRecord::getCheckDate, firstDay)
                        .le(CheckInRecord::getCheckDate, lastDay)
                        .orderByAsc(CheckInRecord::getCheckDate));

        return records.stream()
                .map(r -> r.getCheckDate().toString())
                .collect(Collectors.toList());
    }

    private Integer calcContinuousDays(Long userId, LocalDate today) {
        List<CheckInRecord> records = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .eq(CheckInRecord::getUserId, userId)
                        .orderByDesc(CheckInRecord::getCheckDate));
        if (records.isEmpty()) {
            return 0;
        }
        LocalDate firstDate = records.get(0).getCheckDate();
        if (firstDate.isBefore(today.minusDays(1))) {
            return 0;
        }
        int count = 1;
        for (int i = 1; i < records.size(); i++) {
            if (records.get(i - 1).getCheckDate().minusDays(1).equals(records.get(i).getCheckDate())) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}