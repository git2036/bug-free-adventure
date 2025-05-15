//package com.example.demo.mapper;
//
//import com.example.demo.pojo.Permission;
//import org.apache.ibatis.annotations.*;
//
//import java.util.List;
//
//@Mapper
//public interface PermissionMapper {
//
//    @Select("SELECT * FROM reportpermissions")
//    List<Permission> getAllPermissions();
//
//    @Select("SELECT * FROM reportpermissions WHERE PermissionID = #{permissionId}")
//    Permission getPermissionById(int permissionId);
//
//    @Insert("INSERT INTO reportpermissions (ReportID, UserID, PermissionType) VALUES (#{ReportID}, #{UserID}, #{PermissionType})")
//    @Options(useGeneratedKeys = true, keyProperty = "PermissionID")
//    void addPermission(Permission permission);
//
//    @Delete("DELETE FROM reportpermissions WHERE PermissionID = #{permissionId}")
//    void deletePermission(int permissionId);
//
//    @Update("UPDATE reportpermissions SET ReportID = #{ReportID}, UserID = #{UserID}, PermissionType = #{PermissionType} WHERE PermissionID = #{PermissionID}")
//    void updatePermission(Permission permission);
//}