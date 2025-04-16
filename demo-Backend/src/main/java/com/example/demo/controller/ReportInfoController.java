package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.service.ReportInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reportinfo")
public class ReportInfoController {

    @Autowired
    private ReportInfoService reportInfoService;

    @GetMapping("/datasource/name/{dataSourceID}")
    public Result getDataSourceName(@PathVariable int dataSourceID) {
        String dataSourceName = reportInfoService.getDataSourceName(dataSourceID);
        return Result.success(dataSourceName);
    }

    @GetMapping("/targettable/{templateID}")
    public Result getTargetTable(@PathVariable int templateID) {
        String targetTable = reportInfoService.getTargetTable(templateID);
        return Result.success(targetTable);
    }
}