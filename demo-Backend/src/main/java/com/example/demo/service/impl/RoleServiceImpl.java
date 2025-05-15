package com.example.demo.service.impl;

import com.example.demo.mapper.RoleMapper;
import com.example.demo.pojo.Role;
import com.example.demo.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.getAllRoles();
    }

    @Override
    public Role getRoleById(int roleId) {
        return roleMapper.getRoleById(roleId);
    }

    @Override
    public void addRole(Role role) {
        roleMapper.addRole(role);
    }

    @Override
    public void deleteRole(int roleId) {
        roleMapper.deleteRole(roleId);
    }

    @Override
    public void updateRole(Role role) {
        roleMapper.updateRole(role);
    }

}