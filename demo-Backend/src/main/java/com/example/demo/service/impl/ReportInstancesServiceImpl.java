package com.example.demo.service.impl;

import com.example.demo.mapper.ReportInstancesMapper;
import com.example.demo.pojo.ReportInstances;
import com.example.demo.service.ReportInstancesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportInstancesServiceImpl implements ReportInstancesService {

    @Autowired
    private ReportInstancesMapper reportInstancesMapper;

    @Override
    public int addReportInstance(ReportInstances reportInstances) {
        return reportInstancesMapper.addReportInstance(reportInstances);
    }

    @Override
    public int deleteReportInstance(Integer instanceId) {
        return reportInstancesMapper.deleteReportInstance(instanceId);
    }

    @Override
    public int updateReportInstance(ReportInstances reportInstances) {
        return reportInstancesMapper.updateReportInstance(reportInstances);
    }

    @Override
    public List<ReportInstances> getAllReportInstances() {
        return reportInstancesMapper.getAllReportInstances();
    }

    @Override
    public ReportInstances getReportInstanceById(Integer instanceId) {
        return reportInstancesMapper.getReportInstanceById(instanceId);
    }
}