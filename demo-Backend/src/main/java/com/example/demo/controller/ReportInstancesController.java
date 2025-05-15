package com.example.demo.controller;

import com.example.demo.pojo.ReportInstances;
import com.example.demo.pojo.Result;
import com.example.demo.service.ReportInstancesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reportinstances")
public class ReportInstancesController {

    @Autowired
    private ReportInstancesService reportInstancesService;

    // 新增报表实例
    @PostMapping("/add")
    public Result addReportInstance(@RequestBody ReportInstances reportInstances) {
        int result = reportInstancesService.addReportInstance(reportInstances);
        if (result > 0) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    // 删除报表实例
    @DeleteMapping("/delete/{instanceId}")
    public Result deleteReportInstance(@PathVariable Integer instanceId) {
        int result = reportInstancesService.deleteReportInstance(instanceId);
        if (result > 0) {
            return  Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    // 修改报表实例
    @PutMapping("/update/{instanceID}")
    public Result updateReportInstance(
            @PathVariable("instanceID") Integer instanceID,
            @RequestBody ReportInstances reportInstances
    ) {
        // 确保要修改的ID与路径参数一致
        reportInstances.setInstanceID(instanceID);
        int result = reportInstancesService.updateReportInstance(reportInstances);
        System.out.println(result);
        return result > 0 ? Result.success("修改成功") : Result.error("修改失败");
    }

    // 查询所有报表实例
    @GetMapping("/getAll")
    public Result getAllReportInstances() {
        System.out.println(reportInstancesService.getAllReportInstances());
        return Result.success(reportInstancesService.getAllReportInstances());
    }

    // 根据 ID 查询报表实例
    @GetMapping("/getById/{instanceId}")
    public Result getReportInstanceById(@PathVariable Integer instanceId) {
        return Result.success(reportInstancesService.getReportInstanceById(instanceId));
    }
}