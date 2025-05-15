package com.example.demo.service;

import com.example.demo.pojo.Role;
import com.example.demo.pojo.Users;

import java.util.List;

// 用户服务接口
public interface UsersService {

    Users findByUsername(String username);

    void register(String username, String password);

    Users findById(int id);

    void updateUserPassword(int id, String password);

    void deleteUser(int id);

    void updateUserDataAndRole(int id, String username, String password, String role);

    List<Users> findAllUsers();

    void updateUser(Users user);

    void addUser(Users user);

    //获取用户角色权限
    Role getUserRoleByUsername(String username);
}