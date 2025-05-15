// DataEditionServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.mapper.DataSourcesMapper;
import com.example.demo.pojo.DataSources;
import com.example.demo.pojo.Result;
import com.example.demo.service.DataEditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DataEditionServiceImpl implements DataEditionService {
    private static final Logger logger = LoggerFactory.getLogger(DataEditionServiceImpl.class);
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    @Autowired
    private DataSourcesMapper dataSourcesMapper;

    private Connection getConnection(String dataSourceName) throws SQLException {
        DataSources ds = dataSourcesMapper.findByDataSourceName(dataSourceName);
        if (ds == null) {
            throw new RuntimeException("数据源不存在: " + dataSourceName);
        }
        return DriverManager.getConnection(
                ds.getConnectionInfo(),
                ds.getDataSourceUsername(),
                ds.getDataSourcePassword()
        );
    }

    private void validateIdentifier(String identifier) {
        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException("非法标识符: " + identifier);
        }
    }

    @Override
    public Result addData(String dataSourceName, String targetTable, Map<String, Object> data) {
        try (Connection conn = getConnection(dataSourceName)) {
            validateIdentifier(targetTable);
            data.keySet().forEach(this::validateIdentifier);

            String columns = String.join(",", data.keySet());
            String placeholders = String.join(",", Collections.nCopies(data.size(), "?"));
            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", targetTable, columns, placeholders);

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int i = 1;
                for (Object value : data.values()) {
                    ps.setObject(i++, value);
                }
                int affected = ps.executeUpdate();

                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            return Result.success();
                        }
                    }
                }
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            logger.error("添加数据失败", e);
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    @Override
    public Result updateData(String dataSourceName, String targetTable, String primaryKey, Map<String, Object> data) {
        try (Connection conn = getConnection(dataSourceName)) {
            validateIdentifier(targetTable);
            validateIdentifier(primaryKey);

            if (!data.containsKey(primaryKey)) {
                return Result.error("缺失主键字段: " + primaryKey);
            }

            List<String> setClauses = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!entry.getKey().equals(primaryKey)) {
                    validateIdentifier(entry.getKey());
                    setClauses.add(entry.getKey() + "=?");
                    values.add(entry.getValue());
                }
            }

            String sql = String.format("UPDATE %s SET %s WHERE %s=?",
                    targetTable, String.join(",", setClauses), primaryKey);
            values.add(data.get(primaryKey));

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }
                int affected = ps.executeUpdate();
                return affected > 0 ? Result.success() : Result.error("记录不存在");
            }
        } catch (Exception e) {
            logger.error("更新数据失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public Result deleteData(String dataSourceName, String targetTable, String primaryKey, Object id) {
        try (Connection conn = getConnection(dataSourceName)) {
            validateIdentifier(targetTable);
            validateIdentifier(primaryKey);

            String sql = String.format("DELETE FROM %s WHERE %s=?", targetTable, primaryKey);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                int affected = ps.executeUpdate();
                return affected > 0 ? Result.success() : Result.error("删除失败");
            }
        } catch (Exception e) {
            logger.error("删除数据失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> dynamicQuery(String dataSourceName, String sql) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = getConnection(dataSourceName);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String colName = meta.getColumnLabel(i);
                    if (colName == null || colName.isEmpty()) {
                        colName = meta.getColumnName(i);
                    }
                    row.put(colName, rs.getObject(i));
                }
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("动态查询失败", e);
            throw new RuntimeException("执行查询失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public List<Map<String, String>> getTableMetadata(String dataSourceName, String tableName) {
        List<Map<String, String>> columns = new ArrayList<>();
        try (Connection conn = getConnection(dataSourceName)) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    Map<String, String> column = new HashMap<>();
                    column.put("name", rs.getString("COLUMN_NAME"));
                    column.put("type", rs.getString("TYPE_NAME"));
                    column.put("size", rs.getString("COLUMN_SIZE"));
                    column.put("nullable", rs.getString("NULLABLE"));
                    column.put("remarks", rs.getString("REMARKS"));
                    columns.add(column);
                }
            }
        } catch (Exception e) {
            logger.error("获取元数据失败", e);
            throw new RuntimeException("获取表结构失败: " + e.getMessage());
        }
        return columns;
    }
}