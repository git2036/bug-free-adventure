//package com.example.demo.controller;
//
//import com.example.demo.pojo.Result;
//import com.example.demo.pojo.Permission;
//import com.example.demo.service.PermissionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/permission")
//public class PermissionController {
//
//    @Autowired
//    private PermissionService permissionService;
//
//    @GetMapping("/permissions")
//    public Result getAllPermissions() {
//        List<Permission> permissions = permissionService.getAllPermissions();
//        return Result.success(permissions);
//    }
//
//    @GetMapping("/permissions/{permissionId}")
//    public Result getPermissionById(@PathVariable int permissionId) {
//        Permission permission = permissionService.getPermissionById(permissionId);
//        return Result.success(permission);
//    }
//
//    @PostMapping("/permissions")
//    public Result addPermission(@RequestBody Permission permission) {
//        permissionService.addPermission(permission);
//        return Result.success("权限添加成功");
//    }
//
//    @DeleteMapping("/permissions/{permissionId}")
//    public Result deletePermission(@PathVariable int permissionId) {
//        permissionService.deletePermission(permissionId);
//        return Result.success("权限删除成功");
//    }
//
//    @PutMapping("/permissions/{permissionId}")
//    public Result updatePermission(@PathVariable int permissionId, @RequestBody Permission permission) {
//        permission.setPermissionID(permissionId);
//        permissionService.updatePermission(permission);
//        return Result.success("权限更新成功");
//    }
//}