package com.example.demo.service;

import com.example.demo.pojo.ReportInstances;

import java.util.List;

public interface ReportInstancesService {

    int addReportInstance(ReportInstances reportInstances);

    int deleteReportInstance(Integer instanceId);

    int updateReportInstance(ReportInstances reportInstances);

    List<ReportInstances> getAllReportInstances();

    ReportInstances getReportInstanceById(Integer instanceId);
}