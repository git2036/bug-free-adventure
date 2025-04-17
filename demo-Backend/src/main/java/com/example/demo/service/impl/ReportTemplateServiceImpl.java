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
import java.util.stream.Collectors;

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

        DataSources ds = dataSourcesMapper.findByDataSourceID(template.getDataSourceID());
        if (ds != null) {
            template.setDataSourceName(ds.getDataSourceName());
            template.setTargetTable(parseTableName(template.getQuerySql()));

            // 获取完整主键列表
            List<String> primaryKeys = getPrimaryKeysFromTable(ds, template.getTargetTable());
            // 筛选可用主键（业务策略：优先使用包含"id"的单主键）
            List<String> usablePrimaryKeys = filterUsablePrimaryKeys(primaryKeys);

            template.setPrimaryKeys(primaryKeys);         // 原始主键列表
            template.setUsablePrimaryKeys(usablePrimaryKeys); // 筛选后的可用主键
            System.out.println("可用主键: " + usablePrimaryKeys);
        }

        return template;
    }

    private List<String> getPrimaryKeysFromTable(DataSources ds, String tableName) {
        List<String> primaryKeys = new ArrayList<>();
        String schema = extractSchemaFromJdbcUrl(ds.getConnectionInfo()); // 从URL解析schema

        try (Connection conn = DriverManager.getConnection(
                ds.getConnectionInfo(),
                ds.getDataSourceUsername(),
                ds.getDataSourcePassword())) {

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet pkRs = metaData.getPrimaryKeys(null, schema, tableName); // 使用解析的schema

            while (pkRs.next()) {
                String columnName = pkRs.getString("COLUMN_NAME");
                primaryKeys.add(columnName);
            }

            if (primaryKeys.isEmpty()) {
                throw new RuntimeException("表 " + tableName + " 没有定义主键");
            }

        } catch (Exception e) {
            throw new RuntimeException("获取主键失败: " + e.getMessage(), e);
        }
        return primaryKeys;
    }

    private List<String> filterUsablePrimaryKeys(List<String> primaryKeys) {
        // 业务筛选策略：优先使用包含"id"的单主键（不区分大小写）
        List<String> lowerCaseKeys = primaryKeys.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (lowerCaseKeys.contains("id")) {
            // 找到第一个匹配的"id"字段（严格匹配原字段大小写）
            return primaryKeys.stream()
                    .filter(key -> key.equalsIgnoreCase("id"))
                    .collect(Collectors.toList());
        }
        // 若不包含id且是联合主键，根据业务需求处理（示例：抛出不支持提示）
        else if (primaryKeys.size() > 1) {
            throw new RuntimeException("暂不支持无id的联合主键，当前主键：" + primaryKeys);
        }
        // 单主键且非id，直接返回（需确保业务逻辑支持）
        return primaryKeys;
    }

    private String extractSchemaFromJdbcUrl(String jdbcUrl) {
        // 支持中文和特殊字符的schema解析（排除?和/）
        Pattern pattern = Pattern.compile("jdbc:.*?://.*?/([^?/]+)(\\?|$)");
        Matcher matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("无效的JDBC连接URL，无法解析schema: " + jdbcUrl);
    }

    private List<String> getPrimaryKey(String targetTable, int dataSourceId) {
        DataSources ds = dataSourcesMapper.findByDataSourceID(dataSourceId);
        try (Connection conn = DriverManager.getConnection(
                ds.getConnectionInfo(),
                ds.getDataSourceUsername(),
                ds.getDataSourcePassword()
        )) {
            java.sql.DatabaseMetaData meta = conn.getMetaData();
            ResultSet pkRs = meta.getPrimaryKeys(null, null, targetTable);
            List<String> primaryKeys = new ArrayList<>();
            while (pkRs.next()) {
                primaryKeys.add(pkRs.getString("COLUMN_NAME"));
            }
            if (primaryKeys.isEmpty()) {
                throw new RuntimeException("表 " + targetTable + " 无主键");
            }
            return primaryKeys;
        } catch (SQLException e) {
//            logger.severe("获取主键失败，表名：" + targetTable + "，错误：" + e.getMessage());
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