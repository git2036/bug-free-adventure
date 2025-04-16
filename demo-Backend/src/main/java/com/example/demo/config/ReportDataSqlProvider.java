//package com.example.demo.config;
//
//import com.example.demo.pojo.DataSources;
//import com.example.demo.pojo.ReportDataItem;
//import com.example.demo.service.DataSourcesService;
//import com.example.demo.service.ReportTemplateService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
//@Component
//public class ReportDataSqlProvider {
//
//    @Autowired
//    private ReportTemplateService reportTemplateService;
//
//    @Autowired
//    private DataSourcesService dataSourcesService;
//
//    public String insertReportData(ReportDataItem reportDataItem) {
//        int templateId = reportDataItem.getTemplateId();
//        String tableName = getTableNameFromTemplate(templateId);
//        String databaseName = getDatabaseName(templateId);
//
//        StringBuilder sql = new StringBuilder("INSERT INTO ").append(databaseName).append(".").append(tableName).append(" (template_id");
//        StringBuilder values = new StringBuilder("VALUES (#{templateId}");
//
//        for (Map.Entry<String, Object> entry : reportDataItem.getData().entrySet()) {
//            sql.append(", ").append(entry.getKey());
//            values.append(", #{data.").append(entry.getKey()).append("}");
//        }
//
//        sql.append(") ").append(values).append(")");
//        return sql.toString();
//    }
//
//    public String updateReportData(ReportDataItem reportDataItem) {
//        int templateId = reportDataItem.getTemplateId();
//        String tableName = getTableNameFromTemplate(templateId);
//        String databaseName = getDatabaseName(templateId);
//
//        StringBuilder sql = new StringBuilder("UPDATE ").append(databaseName).append(".").append(tableName).append(" SET ");
//        StringBuilder conditions = new StringBuilder(" WHERE template_id = #{templateId}");
//
//        int index = 0;
//        for (Map.Entry<String, Object> entry : reportDataItem.getData().entrySet()) {
//            if (index > 0) {
//                sql.append(", ");
//            }
//            sql.append(entry.getKey()).append(" = #{data.").append(entry.getKey()).append("}");
//            index++;
//        }
//
//        sql.append(conditions);
//        return sql.toString();
//    }
//
//    public String deleteReportData(int templateId, Map<String, Object> condition) {
//        String tableName = getTableNameFromTemplate(templateId);
//        String databaseName = getDatabaseName(templateId);
//
//        StringBuilder sql = new StringBuilder("DELETE FROM ").append(databaseName).append(".").append(tableName).append(" WHERE template_id = ").append(templateId);
//
//        int index = 0;
//        for (Map.Entry<String, Object> entry : condition.entrySet()) {
//            if (index > 0) {
//                sql.append(" AND ");
//            }
//            sql.append(entry.getKey()).append(" = ").append(entry.getValue());
//            index++;
//        }
//
//        return sql.toString();
//    }
//
//    public String selectReportData(int templateId, Map<String, Object> condition) {
//        String tableName = getTableNameFromTemplate(templateId);
//        String databaseName = getDatabaseName(templateId);
//
//        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(databaseName).append(".").append(tableName).append(" WHERE template_id = ").append(templateId);
//
//        int index = 0;
//        for (Map.Entry<String, Object> entry : condition.entrySet()) {
//            if (index > 0) {
//                sql.append(" AND ");
//            }
//            sql.append(entry.getKey()).append(" = ").append(entry.getValue());
//            index++;
//        }
//
//        return sql.toString();
//    }
//
//    private String getTableNameFromTemplate(int templateId) {
//        // 根据 templateId 获取报表模板信息
//        com.example.demo.pojo.ReportTemplate reportTemplate = reportTemplateService.getReportTemplateById(templateId);
//        if (reportTemplate != null) {
//            String querySql = reportTemplate.getQuerySql();
//            return SqlParser.getTableNameFromSql(querySql);
//        }
//        return "report_data"; // 默认表名
//    }
//
//    private String getDatabaseName(int templateId) {
//        // 根据 templateId 获取报表模板信息
//        com.example.demo.pojo.ReportTemplate reportTemplate = reportTemplateService.getReportTemplateById(templateId);
//        if (reportTemplate != null) {
//            int dataSourceId = reportTemplate.getDataSourceID();
//            DataSources dataSource = dataSourcesService.findByDataSourceID(dataSourceId);
//            if (dataSource != null) {
//                return dataSource.getDataSourceName();
//            }
//        }
//        return "default_database"; // 默认数据库名
//    }
//}