package com.example.demo.controller;

import com.example.demo.pojo.ReportInstances;
import com.example.demo.pojo.ReportTemplate;
import com.example.demo.pojo.Result;
import com.example.demo.service.ReportTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reporttemplates")
public class ReportTemplateController {
    //报表模板相关的控制器


    // 注入ReportTemplateService接口的实现类
    private final ReportTemplateService reportTemplateService;

    // 通过构造函数注入ReportTemplateService
    public ReportTemplateController(ReportTemplateService reportTemplateService) {
        this.reportTemplateService = reportTemplateService;
    }

    // 保存报表模板
    @PostMapping("/save")
    public Result saveReportTemplate(@RequestBody ReportTemplate reportTemplate) {
        System.out.println(reportTemplate);
        // 调用ReportTemplateService的saveReportTemplate方法保存报表模板
        boolean isSuccess = reportTemplateService.saveReportTemplate(reportTemplate);
        if (isSuccess) {
            // 保存成功返回成功信息
            return Result.success("报表模板保存成功");
        } else {
            // 保存失败返回失败信息
            return Result.error("报表模板保存失败");
        }
    }

    // 获取所有报表模板
    @GetMapping("/getAll")
    public Result getAllReportTemplates() {
        // 调用ReportTemplateService的getAllReportTemplates方法获取所有报表模板
        List<ReportTemplate> reportTemplates = reportTemplateService.getAllReportTemplates();
//        System.out.println(reportTemplates);
        System.out.println(reportTemplates);
        return Result.success(reportTemplates);

    }

    // 根据ID获取报表模板
    @GetMapping("/getById/{id}")
    public Result getReportTemplateById(@PathVariable("id") int id) {
        // 调用ReportTemplateService的getReportTemplateById方法根据ID获取报表模板
        return Result.success(reportTemplateService.getReportTemplateById(id));
    }

    // 根据ID删除报表模板
    @DeleteMapping("/deleteById/{id}")
    public Result deleteReportTemplateById(@PathVariable("id") int id) {
        // 调用ReportTemplateService的deleteReportTemplateById方法根据ID删除报表模板
        boolean isSuccess = reportTemplateService.deleteReportTemplateById(id);
        if (isSuccess) {
            // 删除成功返回成功信息
            return Result.success("报表模板删除成功");
        } else {
            // 删除失败返回失败信息
            return Result.error("报表模板删除失败");
        }
    }

    // 根据ID更新报表模板信息
    @PutMapping("/update/{id}")
    public Result updateReportTemplate(@PathVariable("id") int id,@RequestParam("templateName") String templateName, @RequestParam("status") Integer status) {
        // 调用ReportTemplateService的updateReportTemplateStatus方法根据ID更新报表是否启用状态
        boolean isSuccess = reportTemplateService.updateReportTemplate(id,templateName, status);
        if (isSuccess) {
            // 更新成功返回成功信息
            return Result.success("报表模板状态更新成功");
        } else {
            // 更新失败返回失败信息
            return Result.error("报表模板状态更新失败");
        }
    }

    //实例化
    // 实例化报表
    @PostMapping("/instance")
    public Result instanceReport(@RequestBody ReportInstances reportInstance){
        System.out.println(reportInstance);
        return reportTemplateService.instanceReport(reportInstance);
    }
}