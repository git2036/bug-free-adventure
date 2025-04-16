package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.service.DynamicDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 动态数据操作控制器
 * 提供基于报表模板的动态数据增删改查功能
 */
@RestController
@RequestMapping("/dynamic-data")
public class DynamicDataController {

    @Autowired
    private DynamicDataService dynamicDataService;

    /**
     * 添加数据记录
     * @param templateId 报表模板ID（路径变量）
     * @param data 需要添加的数据键值对（请求体）
     * @return 操作结果（成功/失败信息）
     */
    @PostMapping("/{templateId}")
    public Result addData(@PathVariable int templateId,
                          @RequestBody Map<String, Object> data) {
        return dynamicDataService.addData(templateId, data);
    }

    /**
     * 更新数据记录
     * @param templateId 报表模板ID（路径变量）
     * @param data 包含主键和更新字段的键值对（请求体）
     * @return 操作结果（成功/失败信息）
     */
    @PutMapping("/{templateId}")
    public Result updateData(@PathVariable int templateId,
                             @RequestBody Map<String, Object> data) {
        return dynamicDataService.updateData(templateId, data);
    }

    /**
     * 删除数据记录
     * @param templateId 报表模板ID（路径变量）
     * @param id 数据记录主键值（路径变量）
     * @return 操作结果（成功/失败信息）
     */
    @DeleteMapping("/{templateId}/{id}")
    public Result deleteData(@PathVariable int templateId,
                             @PathVariable Object id) {
        return dynamicDataService.deleteData(templateId, id);
    }

    /**
     * 获取数据列表
     * @param templateId 报表模板ID（路径变量）
     * @return 包含数据列表的操作结果
     */
    @GetMapping("/{templateId}")
    public Result getData(@PathVariable int templateId) {
        return Result.success(dynamicDataService.queryData(templateId));
    }

    /**
     * 获取表结构元数据
     * @param templateId 报表模板ID（路径变量）
     * @return 包含字段名称、类型等元数据的操作结果
     */
    @GetMapping("/metadata/{templateId}")
    public Result getMetadata(@PathVariable int templateId) {
        try {
            return Result.success(dynamicDataService.getTableMetadata(templateId));
        } catch (Exception e) {
            return Result.error("获取元数据失败: " + e.getMessage());
        }
    }
}