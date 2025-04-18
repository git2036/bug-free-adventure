package com.example.demo.service;

import com.example.demo.pojo.Result;
import java.util.List;
import java.util.Map;

public interface DynamicDataService {
    Result addData(int templateId, String dataSourceName, String templateKey, Map<String, Object> data);
    Result updateData(int templateId, String dataSourceName, String templateKey, Map<String, Object> data);
    Result deleteData(int templateId, String dataSourceName, String templateKey, int id);

    List<Map<String, Object>> queryData(int templateId);
    List<Map<String, String>> getTableMetadata(int templateId);


}