package com.example.demo.service.impl;

import com.example.demo.mapper.DataSourcesMapper;
import com.example.demo.mapper.ReportTemplateMapper;
import com.example.demo.pojo.DataSources;
import com.example.demo.pojo.ReportTemplate;
import com.example.demo.pojo.Result;
import com.example.demo.service.DynamicDataService;
import com.example.demo.service.ReportTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DynamicDataServiceImpl implements DynamicDataService {

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;
    @Autowired
    private DataSourcesMapper dataSourcesMapper;
    @Autowired
    private ReportTemplateService reportTemplateService;

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataServiceImpl.class);

    private Connection getConnection(int templateId) throws SQLException {
        ReportTemplate template = reportTemplateService.getReportTemplateById(templateId);
        if (template == null) {
            throw new RuntimeException("报表模板不存在");
        }
        DataSources ds = dataSourcesMapper.findByDataSourceID(template.getDataSourceID());
        if (ds == null) {
            throw new RuntimeException("数据源信息不存在");
        }
        return DriverManager.getConnection(
                ds.getConnectionInfo(),
                ds.getDataSourceUsername(),
                ds.getDataSourcePassword());
    }

    private void validateIdentifier(String identifier) {
        if (!Pattern.matches("^[a-zA-Z0-9_]+$", identifier)) {
            throw new IllegalArgumentException("Invalid identifier: " + identifier);
        }
    }

    @Override
    public Result addData(int templateId, String dataSourceName, String templateKey, Map<String, Object> data) {
        try {
            // 获取完整模板（包含目标表信息）
            ReportTemplate template = reportTemplateService.getReportTemplateById(templateId);
            if (template == null) {
                return Result.error("报表模板不存在");
            }

            // 校验目标表合法性
            validateIdentifier(template.getTargetTable());
            // 校验数据字段合法性（防止 SQL 注入）
            data.keySet().forEach(this::validateIdentifier);

            try (Connection conn = getConnection(templateId)) {
                // 构建插入语句
                String columns = String.join(",", data.keySet());
                String placeholders = data.keySet().stream()
                        .map(k -> "?")
                        .collect(Collectors.joining(","));
                String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                        template.getTargetTable(), columns, placeholders);

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int i = 1;
                    for (Object value : data.values()) {
                        ps.setObject(i++, value); // 按顺序设置参数
                    }
                    int affected = ps.executeUpdate();
                    return affected > 0 ? Result.success("数据添加成功") : Result.error("添加失败，无记录受影响");
                }
            }
        } catch (IllegalArgumentException e) { // 标识符非法（字段名/表名包含非法字符）
            return Result.error("非法字段名：" + e.getMessage());
        } catch (Exception e) { // 其他异常（如数据库连接失败）
            logger.error("添加数据失败，模板ID：{}，错误：{}", templateId, e.getMessage(), e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateData(int templateId, String dataSourceName, String templateKey, Map<String, Object> data) {
        try {
            ReportTemplate template = reportTemplateService.getFullTemplate(templateId);

            if (template == null) {
                return Result.error("模板不存在");
            }

            // 获取实际的主键字段（从模板信息中获取）
            String primaryKey = template.getPrimaryKey();
            logger.info("更新数据，模板ID：{}，目标表：{}，主键：{}",
                    templateId, template.getTargetTable(), primaryKey);

            // 校验主键字段合法性
            validateIdentifier(primaryKey);

            // 新增：打印关键信息以便调试
            logger.info("更新数据，模板ID：{}，目标表：{}，主键：{}",
                    templateId, template.getTargetTable(), template.getPrimaryKey());

            // 校验目标表和主键是否存在（避免 null）
            if (template.getTargetTable() == null || template.getPrimaryKey() == null) {
                return Result.error("报表模板配置错误：缺少目标表或主键信息");
            }

            validateIdentifier(template.getTargetTable());
            validateIdentifier(template.getPrimaryKey().toString());

            if (!data.containsKey(primaryKey)) {
                // 新增：打印缺失的主键字段和数据内容
                logger.warn("更新数据时缺失主键字段：{}，传入数据：{}", primaryKey, data);
                return Result.error("缺失主键字段: " + primaryKey);
            }
            Object pkValue = data.get(primaryKey);
            System.out.println("pkValue: " + pkValue);
            System.out.println("解析出的主键：" + primaryKey);
            System.out.println("Target Table: " + template.getTargetTable());

            try (Connection conn = getConnection(templateId)) {
                List<String> setClauses = data.keySet().stream()
                        .filter(k -> !k.equals(primaryKey))
                        .map(k -> k + "=?").collect(Collectors.toList());

                String sql = String.format("UPDATE %s SET %s WHERE %s=?",
                        template.getTargetTable(),
                        String.join(",", setClauses),
                        primaryKey);

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int i = 1;
                    for (String key : data.keySet()) {
                        if (!key.equals(primaryKey)) {
                            ps.setObject(i++, data.get(key));
                        }
                    }
                    ps.setObject(i, pkValue);
                    int affected = ps.executeUpdate();
                    return affected > 0 ? Result.success() : Result.error("记录不存在");
                }
            }
        } catch (Exception e) {
            logger.error("更新数据失败，模板ID：{}，错误：{}", templateId, e.getMessage(), e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }




    @Override
    public Result deleteData(int templateId, String dataSourceName, String templateKey, int primaryKeyValue) {
        System.out.println(templateId+" "+dataSourceName+" "+templateKey+" "+primaryKeyValue);
        try {
            // 获取完整模板（包含主键信息）
            ReportTemplate template = reportTemplateService.getFullTemplate(templateId);
            if (template == null) {
                return Result.error("报表模板不存在");
            }

            // 校验目标表和主键合法性
            validateIdentifier(template.getTargetTable());
            validateIdentifier(templateKey); // 主键字段名必须合法

//            // 校验主键值非空（防止删除条件无效）
//            if (primaryKeyValue == null) {
//                return Result.error("主键值为空，无法删除");
//            }

            try (Connection conn = getConnection(templateId)) {
                // 构建删除语句（使用主键作为条件）
                String sql = String.format("DELETE FROM %s WHERE %s=?",
                        template.getTargetTable(), templateKey);

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setObject(1, primaryKeyValue); // 设置主键值
                    int affected = ps.executeUpdate();
                    return affected > 0 ? Result.success("数据删除成功") : Result.error("删除失败，记录不存在");
                }
            }
        } catch (IllegalArgumentException e) { // 标识符非法
            return Result.error("非法表名或主键名：" + e.getMessage());
        } catch (Exception e) { // 其他异常
            logger.error("删除数据失败，模板ID：{}，错误：{}", templateId, e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }



    @Override
    public List<Map<String, Object>> queryData(int templateId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ReportTemplate template = reportTemplateService.getReportTemplateById(templateId);

        if (template == null) {
            throw new RuntimeException("报表模板不存在");
        }

        String querySql = template.getQuerySql();
        if (querySql == null || querySql.trim().isEmpty()) {
            throw new RuntimeException("查询SQL未配置");
        }

        try (Connection conn = getConnection(templateId);
             PreparedStatement ps = conn.prepareStatement(querySql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i); // 获取列别名（如果有）
                    if (columnName == null || columnName.isEmpty()) {
                        columnName = metaData.getColumnName(i); // 原始列名
                    }
                    row.put(columnName, rs.getObject(i));
                }
                resultList.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("执行查询失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("系统错误: " + e.getMessage(), e);
        }

        return resultList;
    }

    @Override
    public List<Map<String, String>> getTableMetadata(int templateId) {
        try {
            ReportTemplate template = reportTemplateService.getReportTemplateById(templateId);
            try (Connection conn = getConnection(templateId)) {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet rs = meta.getColumns(null, null, template.getTargetTable(), null);

                List<Map<String, String>> columns = new ArrayList<>();
                while (rs.next()) {
                    Map<String, String> column = new HashMap<>();
                    column.put("name", rs.getString("COLUMN_NAME"));
                    column.put("type", rs.getString("TYPE_NAME"));
                    column.put("size", rs.getString("COLUMN_SIZE"));
                    columns.add(column);
                }
                return columns;
            }
        } catch (Exception e) {
            throw new RuntimeException("获取元数据失败", e);
        }
    }
}