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

        // 获取数据源名称和目标表（原有逻辑）
        DataSources ds = dataSourcesMapper.findByDataSourceID(template.getDataSourceID());
        if (ds != null) {
            template.setDataSourceName(ds.getDataSourceName());
            template.setTargetTable(parseTableName(template.getQuerySql()));
        }

        // 新增：获取表的主键字段
        String primaryKey = getPrimaryKeyFromTable(ds, template.getTargetTable());
        template.setPrimaryKey(primaryKey);
        System.out.println("主键: " + primaryKey);

        return template;
    }

    private String getPrimaryKeyFromTable(DataSources ds, String tableName) {
        try (Connection conn = DriverManager.getConnection(
                ds.getConnectionInfo(),
                ds.getDataSourceUsername(),
                ds.getDataSourcePassword())) {

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet pkRs = metaData.getPrimaryKeys(null, null, tableName);
            if (pkRs.next()) {
                return pkRs.getString("COLUMN_NAME"); // 获取单个主键字段
            }
        } catch (Exception e) {
            throw new RuntimeException("获取主键失败", e);
        }
        return null; // 若表无主键，需根据业务处理（如抛出异常或默认处理）
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