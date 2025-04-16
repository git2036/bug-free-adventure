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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReportTemplateServiceImpl implements ReportTemplateService {

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Autowired
    private DataSourcesMapper dataSourcesMapper;

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataServiceImpl.class);

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

        // 第一步：解析目标表（确保非空）
        String targetTable = parseTableName(template.getQuerySql());
        System.out.println("目标表: " + targetTable);
        if (targetTable == null) {
            throw new RuntimeException("SQL 中未找到有效的目标表"); // 或日志提示
        }
        template.setTargetTable(targetTable);

        // 第二步：获取数据源并处理主键
        DataSources ds = dataSourcesMapper.findByDataSourceID(template.getDataSourceID());
        if (ds != null) {
            template.setDataSourceName(ds.getDataSourceName());
            // 获取主键（此时 targetTable 已正确解析）
            String primaryKey = getPrimaryKey(targetTable, ds.getDataSourceID());
            template.setPrimaryKey(primaryKey); // 设置主键
        }

        return template;
    }

    private String getPrimaryKey(String targetTable, int dataSourceId) {
        DataSources ds = dataSourcesMapper.findByDataSourceID(dataSourceId);
        try (Connection conn = DriverManager.getConnection(
                ds.getConnectionInfo(),
                ds.getDataSourceUsername(),
                ds.getDataSourcePassword()
        )) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet pkRs = meta.getPrimaryKeys(null, null, targetTable); // 传入正确的 targetTable
            List<String> primaryKeys = new ArrayList<>();
            while (pkRs.next()) {
                primaryKeys.add(pkRs.getString("COLUMN_NAME"));
            }
            if (primaryKeys.isEmpty()) {
                throw new RuntimeException("表 " + targetTable + " 无主键"); // 明确抛出异常
            }
            return primaryKeys.get(0); // 单主键场景，多主键需特殊处理
        } catch (SQLException e) {
            logger.error("获取主键失败，表名：{}，错误：{}", targetTable, e.getMessage());
            throw new RuntimeException("获取主键失败", e);
        }
    }

    // 优化后的 SQL 表名解析方法（支持别名和复杂语法）
    private static String parseTableName(String sql) {
        // 匹配 FROM 后的表名，支持基础场景（无引号、无别名）和简单模式名（如 schema.table）
        Pattern pattern = Pattern.compile(
                "(?i)\\bFROM\\s+([a-zA-Z_][a-zA-Z0-9_.]*)", // 核心匹配：FROM + 表名（允许 . 用于模式名）
                Pattern.COMMENTS | Pattern.MULTILINE
        );
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            // 去除可能的干扰字符（保留 . 用于模式名，仅删除危险符号）
            return tableName.replaceAll("[;`'\"()]", "");
        }
        return null;
    }
}