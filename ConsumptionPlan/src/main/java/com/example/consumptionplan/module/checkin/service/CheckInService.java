package com.example.consumptionplan.module.checkin.service;

import com.example.consumptionplan.module.checkin.vo.CheckInVO;

import java.util.List;

public interface CheckInService {

    void checkIn(Long userId);

    CheckInVO getCheckInVO(Long userId);

    List<String> listCheckedDates(Long userId, String month);
}