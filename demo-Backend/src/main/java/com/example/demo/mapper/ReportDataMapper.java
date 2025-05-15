package com.example.demo.mapper;

//import com.example.demo.config.ReportDataSqlProvider;
import com.example.demo.pojo.ReportDataItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportDataMapper {



    int insertReportData(ReportDataItem reportDataItem);

    int updateReportData(ReportDataItem reportDataItem);

    int deleteReportData(int templateId, Map<String, Object> condition);

    List<Map<String, Object>> selectReportData(int templateId, Map<String, Object> condition);
}

