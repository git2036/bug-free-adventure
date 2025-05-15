package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.Role;
import com.example.demo.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/roles")
    public Result getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    @GetMapping("/roles/{roleId}")
    public Result getRoleById(@PathVariable int roleId) {
        Role role = roleService.getRoleById(roleId);
        return Result.success(role);
    }

    @PostMapping("/roles")
    public Result addRole(@RequestBody Role role) {
        roleService.addRole(role);
        return Result.success("角色添加成功");
    }

    @DeleteMapping("/roles/{roleId}")
    public Result deleteRole(@PathVariable int roleId) {
        roleService.deleteRole(roleId);
        return Result.success("角色删除成功");
    }

    @PutMapping("/roles/{roleId}")
    public Result updateRole(@PathVariable Integer roleId, @RequestBody Role role) {
        role.setRoleID(roleId);
        roleService.updateRole(role);
        return Result.success("角色更新成功");
    }

}