package com.example.consumptionplan.module.report.service;

import com.example.consumptionplan.module.report.vo.ReportVO;

import java.util.List;

public interface ReportService {

    ReportVO generateReport(Long userId, String reportMonth);

    ReportVO getReport(Long userId, String reportMonth);

    List<ReportVO> listReports(Long userId);
}
