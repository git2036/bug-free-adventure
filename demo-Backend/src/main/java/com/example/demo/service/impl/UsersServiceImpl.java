package com.example.demo.service.impl;

import com.example.demo.mapper.RoleMapper;
import com.example.demo.pojo.Role;
import com.example.demo.pojo.Users;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// 标识该类为一个服务类
@Service
public class UsersServiceImpl implements UsersService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Users findByUsername(String username) {
        return usersMapper.findByUsername(username);
    }

    @Override
    public void register(String username, String password) {
        usersMapper.add(username, password);
    }

    @Override
    public Users findById(int id) {
        return usersMapper.findByUserId(id);
    }

    @Override
    public void updateUserPassword(int id, String password) {
        usersMapper.updateUserPassword(id, password);
    }

    @Override
    public void deleteUser(int id) {
        usersMapper.deleteUser(id);
    }

    @Override
    public void updateUserDataAndRole(int id, String username, String password, String role) {
        usersMapper.updateUserDataAndRole(id, username,  password, role);
    }

    @Override
    public List<Users> findAllUsers() {
        return usersMapper.findAllUsers();
    }

    @Override
    public void updateUser(Users user) {
        usersMapper.updateUser(user);
    }

    @Override
    public void addUser(Users user) {
        usersMapper.addUser(user);
    }

    @Override
    public Role getUserRoleByUsername(String username) {
        Users user = usersMapper.findByUsername(username); // 根据用户名查询用户
        if (user != null) {
            return roleMapper.getRoleByName(user.getRole()); // 根据角色名查询角色权限
        }
        return null;
    }
}