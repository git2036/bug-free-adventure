package com.example.demo.service.impl;

import com.example.demo.mapper.DataSourcesMapper;
import com.example.demo.mapper.ReportTemplateMapper;
import com.example.demo.service.ReportInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportInfoServiceImpl implements ReportInfoService {

    @Autowired
    private DataSourcesMapper dataSourcesMapper;

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Override
    public String getDataSourceName(int dataSourceID) {
        return dataSourcesMapper.getDataSourceNameById(dataSourceID);
    }

    @Override
    public String getTargetTable(int templateID) {
        String querySql = reportTemplateMapper.getQuerySqlById(templateID);
        if (querySql == null) {
            return null;
        }
        // 简单的正则表达式匹配，用于提取 SQL 语句中的表名
        Pattern pattern = Pattern.compile("FROM\\s+(\\w+)");
        Matcher matcher = pattern.matcher(querySql.toUpperCase());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}