package com.example.demo.pojo;

import lombok.Data;

@Data
public class ReportInstances {
    private Integer instanceID;
    private String instanceName;
    private Integer templateID;
    private String reportData;
    private String createdBy;
    private String status;
}