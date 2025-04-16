package com.example.demo.service;

import com.example.demo.pojo.Result;
import java.util.List;
import java.util.Map;

public interface DynamicDataService {
    Result addData(int templateId, Map<String, Object> data);
    Result updateData(int templateId, Map<String, Object> data);
    Result deleteData(int templateId, Object primaryKeyValue);
    List<Map<String, Object>> queryData(int templateId);
    List<Map<String, String>> getTableMetadata(int templateId);
}