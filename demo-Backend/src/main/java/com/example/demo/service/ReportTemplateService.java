package com.example.demo.service;

import com.example.demo.pojo.ReportInstances;
import com.example.demo.pojo.ReportTemplate;
import com.example.demo.pojo.Result;

import java.util.List;

public interface ReportTemplateService {
    boolean saveReportTemplate(ReportTemplate reportTemplate);
    List<ReportTemplate> getAllReportTemplates();

    ReportTemplate getReportTemplateById(int id);

    boolean deleteReportTemplateById(int id);

    boolean updateReportTemplate(int id, String templateName, Integer status);

    ReportTemplate getFullTemplate(int templateId);


    Result instanceReport(ReportInstances reportInstance);
}