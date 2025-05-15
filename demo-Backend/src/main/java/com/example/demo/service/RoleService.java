package com.example.demo.service;

import com.example.demo.pojo.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role getRoleById(int roleId);
    void addRole(Role role);
    void deleteRole(int roleId);
    void updateRole(Role role);
}