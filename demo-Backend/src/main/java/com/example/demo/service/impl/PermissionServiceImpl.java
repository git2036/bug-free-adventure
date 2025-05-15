//package com.example.demo.service.impl;
//
//import com.example.demo.mapper.PermissionMapper;
//import com.example.demo.pojo.Permission;
//import com.example.demo.service.PermissionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class PermissionServiceImpl implements PermissionService {
//
//    @Autowired
//    private PermissionMapper permissionMapper;
//
//    @Override
//    public List<Permission> getAllPermissions() {
//        return permissionMapper.getAllPermissions();
//    }
//
//    @Override
//    public Permission getPermissionById(int permissionId) {
//        return permissionMapper.getPermissionById(permissionId);
//    }
//
//    @Override
//    public void addPermission(Permission permission) {
//        permissionMapper.addPermission(permission);
//    }
//
//    @Override
//    public void deletePermission(int permissionId) {
//        permissionMapper.deletePermission(permissionId);
//    }
//
//    @Override
//    public void updatePermission(Permission permission) {
//        permissionMapper.updatePermission(permission);
//    }
//}