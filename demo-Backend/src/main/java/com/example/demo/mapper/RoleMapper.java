package com.example.demo.mapper;

import com.example.demo.pojo.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Select("SELECT * FROM roles")
    List<Role> getAllRoles();

    @Select("SELECT * FROM roles WHERE RoleID = #{roleId}")
    Role getRoleById(int roleId);

    @Insert("INSERT INTO roles (RoleName, Permissions) VALUES (#{RoleName}, #{Permissions})")
    @Options(useGeneratedKeys = true, keyProperty = "RoleID")
    void addRole(Role role);

    @Delete("DELETE FROM roles WHERE RoleID = #{roleId}")
    void deleteRole(int roleId);

    @Update("UPDATE roles SET RoleName = #{RoleName}, Permissions = #{Permissions} WHERE RoleID = #{RoleID}")
    void updateRole(Role role);

    @Select("SELECT * FROM roles WHERE RoleName = #{roleName}")
    Role getRoleByName(String roleName);
}