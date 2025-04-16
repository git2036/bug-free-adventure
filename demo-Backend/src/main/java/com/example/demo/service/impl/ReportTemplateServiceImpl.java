package com.example.demo.service.impl;

import com.example.demo.mapper.DataSourcesMapper;
import com.example.demo.mapper.ReportTemplateMapper;
import com.example.demo.pojo.DataSources;
import com.example.demo.pojo.ReportTemplate;
import com.example.demo.service.ReportTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
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
            // 新增：通过数据源连接获取主键
            String primaryKey = getPrimaryKey(template.getTargetTable(), ds.getDataSourceID());
            template.setPrimaryKey(primaryKey); // 设置主键
        }

        // 解析SQL获取目标表（已有逻辑）
        template.setTargetTable(parseTableName(template.getQuerySql()));

        return template;
    }

    // 新增：获取主键的辅助方法
    private String getPrimaryKey(String targetTable, int dataSourceId) {
        DataSources ds = dataSourcesMapper.findByDataSourceID(dataSourceId);
        try (Connection conn = DriverManager.getConnection(
                ds.getConnectionInfo(),
                ds.getDataSourceUsername(),
                ds.getDataSourcePassword()
        )) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet pkRs = meta.getPrimaryKeys(null, null, targetTable);
            List<String> primaryKeys = new ArrayList<>();
            while (pkRs.next()) {
                primaryKeys.add(pkRs.getString("COLUMN_NAME"));
            }
            return primaryKeys.isEmpty() ? null : primaryKeys.get(0); // 单主键场景
        } catch (SQLException e) {
            throw new RuntimeException("获取主键失败: " + e.getMessage());
        }
    }

    // 优化后的 SQL 表名解析方法（支持别名和复杂语法）
    private String parseTableName(String sql) {
        if (sql == null) return null;
        // 匹配 FROM 后的表名（忽略大小写，支持别名和子查询）
        Pattern pattern = Pattern.compile(
                "(?i)\\bFROM\\b\\s+(?:\\[?\"?([a-zA-Z_][a-zA-Z0-9_]*)\"?\\]?\\s+AS\\s+\\w+\\s*|\\[?\"?([a-zA-Z_][a-zA-Z0-9_]*)\"?\\]?)",
                Pattern.COMMENTS
        );
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            // 优先获取带 AS 别名前的表名，其次直接表名
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        return null;
    }
}