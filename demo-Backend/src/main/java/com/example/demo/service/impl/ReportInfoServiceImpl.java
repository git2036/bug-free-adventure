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
        if (querySql == null || querySql.trim().isEmpty()) {
            return null;
        }
        // 支持模式名（db.user）、别名（user AS u），忽略大小写，提取第一个表名
        Pattern pattern = Pattern.compile(
                "(?i)FROM\\s+([a-zA-Z0-9_.]+)(?:\\s+AS\\s+[a-zA-Z0-9_]+)?",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(querySql);
        return matcher.find() ? matcher.group(1).replaceAll("['\"]", "") : null;
    }
}