package com.example.demo.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data//生成getter、setter方法、toString方法、equals方法、hashCode方法
@JsonIgnoreProperties(ignoreUnknown = true)//忽略未知属性
public class ReportTemplate {
    private Integer templateID;
    private String templateName;
    private Integer dataSourceID;
    private String templateCreator;
    private String querySql;
    private String templateConfig;
    private String templateState;


    @Transient // 表示非数据库字段
    private String dataSourceName;

    @Transient
    private String targetTable;

    @Transient
    private String primaryKey;

    @Transient
    private String usablePrimaryKey;

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getUsablePrimaryKey() {
        return usablePrimaryKey;
    }

    public void setUsablePrimaryKey(String usablePrimaryKey) {
        this.usablePrimaryKey = usablePrimaryKey;
    }
}