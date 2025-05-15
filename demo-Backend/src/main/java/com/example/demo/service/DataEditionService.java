// DataEditionService.java
package com.example.demo.service;

import com.example.demo.pojo.Result;
import java.util.List;
import java.util.Map;

public interface DataEditionService {
    Result addData(String dataSourceName, String targetTable, Map<String, Object> data);
    Result updateData(String dataSourceName, String targetTable, String primaryKey, Map<String, Object> data);
    Result deleteData(String dataSourceName, String targetTable, String primaryKey, Object id);
    List<Map<String, Object>> dynamicQuery(String dataSourceName, String sql);
    List<Map<String, String>> getTableMetadata(String dataSourceName, String tableName);
}