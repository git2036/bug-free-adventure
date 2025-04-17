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
import java.util.Optional;
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

            List<String> primaryKeys = getPrimaryKeysFromTable(ds, template.getTargetTable());
            String usablePrimaryKey = filterUsablePrimaryKey(primaryKeys);

            // 设置主键字段（取第一个有效主键）
            template.setPrimaryKey(usablePrimaryKey != null ? usablePrimaryKey : primaryKeys.get(0));
        }

        return template;
    }

    private String filterUsablePrimaryKey(List<String> primaryKeys) {
        if (primaryKeys.isEmpty()) {
            throw new RuntimeException("表无主键");
        }

        // 优先选择名为 "id" 的主键
        Optional<String> idKey = primaryKeys.stream()
                .filter(key -> key.equalsIgnoreCase("id"))
                .findFirst();

        return idKey.orElse(primaryKeys.get(0));
    }

    private List<String> getPrimaryKeysFromTable(DataSources ds, String tableName) {
        List<String> primaryKeys = new ArrayList<>();
        String schema = extractSchemaFromJdbcUrl(ds.getConnectionInfo());
        try (Connection conn = DriverManager.getConnection(
                ds.getConnectionInfo(),
                ds.getDataSourceUsername(),
                ds.getDataSourcePassword())) {

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet pkRs = metaData.getPrimaryKeys(null, schema, tableName);

            while (pkRs.next()) {
                primaryKeys.add(pkRs.getString("COLUMN_NAME"));
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
        List<String> lowerCaseKeys = primaryKeys.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (lowerCaseKeys.contains("id")) {
            return primaryKeys.stream()
                    .filter(key -> key.equalsIgnoreCase("id"))
                    .collect(Collectors.toList());
        } else if (primaryKeys.size() > 1) {
            throw new RuntimeException("暂不支持无id的联合主键，当前主键：" + primaryKeys);
        }
        return primaryKeys;
    }

    private String extractSchemaFromJdbcUrl(String jdbcUrl) {
        Pattern pattern = Pattern.compile("jdbc:.*?://.*?/([^?/]+)(\\?|$)");
        Matcher matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("无效的JDBC连接URL，无法解析schema: " + jdbcUrl);
    }

    private String parseTableName(String sql) {
        Pattern pattern = Pattern.compile(
                "(?i)\\bFROM\\s+([a-zA-Z_][a-zA-Z0-9_.]*)",
                Pattern.COMMENTS | Pattern.MULTILINE
        );
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            return tableName.replaceAll("[;`'\"()]", "");
        }
        return null;
    }
}