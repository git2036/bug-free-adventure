// DataEditionController.java
package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.service.DataEditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data-edition")
public class DataEditionController {

    @Autowired
    private DataEditionService dataEditionService;


    @PostMapping
    public Result addData(
            @RequestHeader("dataSourceName") String dataSourceName,
            @RequestHeader("targetTable") String targetTable,
            @RequestBody Map<String, Object> data) {
        return dataEditionService.addData(dataSourceName, targetTable, data);
    }

    @PutMapping
    public Result updateData(
            @RequestHeader("dataSourceName") String dataSourceName,
            @RequestHeader("targetTable") String targetTable,
            @RequestHeader("primaryKey") String primaryKey,
            @RequestBody Map<String, Object> data) {
        return dataEditionService.updateData(dataSourceName, targetTable, primaryKey, data);
    }

    @DeleteMapping("/{id}")
    public Result deleteData(
            @RequestHeader("dataSourceName") String dataSourceName,
            @RequestHeader("targetTable") String targetTable,
            @RequestHeader("primaryKey") String primaryKey,
            @PathVariable Object id) {
        return dataEditionService.deleteData(dataSourceName, targetTable, primaryKey, id);
    }

    @GetMapping("/query")
    public Result queryData(
            @RequestHeader("dataSourceName") String dataSourceName,
            @RequestParam("sql") String sql) {
        return Result.success(dataEditionService.dynamicQuery(dataSourceName, sql));
    }

    @GetMapping("/metadata/{tableName}")
    public Result getMetadata(
            @RequestHeader("dataSourceName") String dataSourceName,
            @PathVariable String tableName) {
        try {
            return Result.success(dataEditionService.getTableMetadata(dataSourceName, tableName));
        } catch (Exception e) {
            return Result.error("获取元数据失败: " + e.getMessage());
        }
    }
}