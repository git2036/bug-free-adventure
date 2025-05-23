package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.Role;
import com.example.demo.pojo.Users;
import com.example.demo.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UsersController {
    //用户相关的控制器

    // 注入UsersService
    @Autowired
    private UsersService usersService;

    // 创建用户
    @PostMapping("/register")
    public Result registerUsers(@RequestParam("username") String username, @RequestParam("password") String password) {
        Users u = usersService.findByUsername(username);
        if (u == null) {
            usersService.register(username, password);
            return Result.success();
        } else {
            return Result.error("用户名已存在");
        }

    }

    // 登录
    // 处理登录请求
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // 根据用户名查找用户
        Users u = usersService.findByUsername(username);
        // 如果用户存在且密码正确，返回用户信息
        if (u != null && u.getPassword().equals(password)) {
            return Result.success(u);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    // 查询用户信息
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable("id") int id) {
        Users u = usersService.findById(id);
        if (u != null) {
            return Result.success(u);
        } else {
            return Result.error("用户不存在");
        }
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public Result updateUser(@PathVariable("id") int id, @RequestBody Users user) {
        Users u = usersService.findById(id);
        if (u != null) {
            usersService.updateUser(user);
            return Result.success();
        } else {
            return Result.error("用户不存在");
        }
    }

    // 更新用户密码
    @PutMapping("/{id}/password")
    public Result updateUserPassword(@PathVariable("id") int id, @RequestParam("password") String password) {
        Users u = usersService.findById(id);
        if (u != null) {
            usersService.updateUserPassword(id, password);
            return Result.success();
        } else {
            return Result.error("用户不存在");
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable("id") int id) {
        Users u = usersService.findById(id);
        if (u != null) {
            usersService.deleteUser(id);
            return Result.success();
        } else {
            return Result.error("用户不存在");
        }
    }

    //更新用户信息和权限
    @PutMapping("/{id}/authority")
    public Result updateUserAuthority(@PathVariable("id") int id, @RequestBody Users user) {
        //取出 username, password, role
        String username = user.getUsername();
        String password = user.getPassword();
        String role = user.getRole();

        Users u = usersService.findById(id);
        if (u != null) {
            usersService.updateUserDataAndRole(id,  username,  password,  role);
            return Result.success();
        } else {
            return Result.error("用户不存在");
        }
    }

    //查询所有用户信息
    @GetMapping("/all")
    public Result getAllUsers() {
        return Result.success(usersService.findAllUsers());
    }

    //新增用户
    @PostMapping("/adduser")
    public Result addUser(@RequestBody Users user) {
        usersService.addUser(user);
        return Result.success();
    }

    @GetMapping("/{username}/permissions")
    public Result getUserPermissions(@PathVariable("username") String username) {
        System.out.println(username);
        Role role = usersService.getUserRoleByUsername(username);
        System.out.println(role);
        if (role != null) {
            return Result.success(role.getPermissions());
        } else {
            return Result.error("未找到用户角色信息");
        }
    }
}