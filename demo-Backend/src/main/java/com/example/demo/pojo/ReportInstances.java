package com.example.demo.pojo;

import lombok.Data;

@Data
public class ReportInstances {
    private Integer InstanceID;
    private String InstanceName;
    private Integer TemplateID;
    private String ReportData;
    private Integer CreatedBy;
    private String Status;
}
