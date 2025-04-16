package com.example.demo.service.impl;

import com.example.demo.mapper.DataSourcesMapper;
import com.example.demo.mapper.ReportTemplateMapper;
import com.example.demo.pojo.DataSources;
import com.example.demo.pojo.ReportTemplate;
import com.example.demo.service.ReportTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportTemplateServiceImpl implements ReportTemplateService {

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Autowired
    private DataSourcesMapper dataSourcesMapper;

    @Override
    public boolean saveReportTemplate(ReportTemplate reportTemplate) {
        int rows = reportTemplateMapper.insertReportTemplate(reportTemplate);
        return rows > 0;
    }

    @Override
    public List<ReportTemplate> getAllReportTemplates() {
        return reportTemplateMapper.getAllReportTemplates();
    }

    @Override
    public ReportTemplate getReportTemplateById(int id) {
        return reportTemplateMapper.getReportTemplateById(id);
    }

    @Override
    public boolean deleteReportTemplateById(int id) {
        int rows = reportTemplateMapper.deleteReportTemplateById(id);
        return rows > 0;
    }

    @Override
    public boolean updateReportTemplate(int id, boolean status) {
        int rows = reportTemplateMapper.updateReportTemplate(id);
        return rows > 0;
    }


    @Override
    public ReportTemplate getFullTemplate(int templateId) {
        ReportTemplate template = reportTemplateMapper.getReportTemplateById(templateId);
        if (template == null) return null;

        // 获取数据源名称
        DataSources ds = dataSourcesMapper.findByDataSourceID(template.getDataSourceID());
        if (ds != null) {
            template.setDataSourceName(ds.getDataSourceName());
        }

        // 解析SQL获取目标表
        template.setTargetTable(parseTableName(template.getQuerySql()));

        return template;
    }

    // SQL解析工具方法
    private String parseTableName(String sql) {
        // 简单解析FROM子句后的第一个表名
        Pattern pattern = Pattern.compile("(?i)FROM\\s+([^\\s,)(]+)");
        Matcher matcher = pattern.matcher(sql);
        return matcher.find() ? matcher.group(1).replaceAll("[;`'\"]", "") : null;
    }
}