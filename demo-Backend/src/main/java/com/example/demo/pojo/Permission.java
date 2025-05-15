package com.example.demo.pojo;

import lombok.Data;

@Data
public class Permission {
    private Integer PermissionID;
    private Integer ReportID;
    private Integer UserID;
    private String PermissionType;
}