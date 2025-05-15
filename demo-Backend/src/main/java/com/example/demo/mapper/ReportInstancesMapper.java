package com.example.demo.mapper;

import com.example.demo.pojo.ReportInstances;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReportInstancesMapper {

    // 根据 InstanceID 查询报表实例
    @Select("select * from reportinstances where InstanceID = #{instanceId}")
    ReportInstances getReportInstanceById(int instanceId);

    // 插入报表实例
    @Insert("insert into reportinstances (InstanceName,TemplateID, ReportData, CreatedBy, Status) values (#{InstanceName},#{TemplateID}, #{ReportData}, #{CreatedBy}, #{Status})")
    int addReportInstance(ReportInstances reportInstances);

    // 查询所有报表实例
    @Select("select * from reportinstances")
    List<ReportInstances> getAllReportInstances();

    // 根据 TemplateID 查询报表实例
    @Select("select * from reportinstances where TemplateID = #{templateId}")
    List<ReportInstances> findByTemplateID(int templateId);

    // 更新报表实例信息
    @Update("update reportinstances set InstanceName = #{InstanceName},TemplateID = #{TemplateID}, ReportData = #{ReportData}, CreatedBy = #{CreatedBy}, Status = #{Status} where InstanceID = #{InstanceID}")
    int updateReportInstance(ReportInstances reportInstances);

    // 删除报表实例
    @Delete("delete from reportinstances where InstanceID = #{instanceId}")
    int deleteReportInstance(int instanceId);

    // 根据 CreatedBy 查询报表实例
    @Select("select * from reportinstances where CreatedBy = #{createdBy}")
    List<ReportInstances> findByCreatedBy(int createdBy);

    // 根据 Status 查询报表实例
    @Select("select * from reportinstances where Status = #{status}")
    List<ReportInstances> findByStatus(String status);

}