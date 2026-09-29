package com.example.system.controller;

import com.example.common.annotation.Log;
import com.example.common.core.Result;
import com.example.common.enums.BusinessType;
import com.example.system.domain.dto.PasswordUpdateDTO;
import com.example.system.domain.dto.ProfileUpdateDTO;
import com.example.system.domain.dto.UserSaveDTO;
import com.example.system.domain.entity.SysUser;
import com.example.system.domain.vo.UserInfoVO;
import com.example.system.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/profile")
    public Result<UserInfoVO> profile() {
        return Result.success(userService.getCurrentUserInfo());
    }

    @PutMapping("/profile")
    @Log(title = "个人资料", businessType = BusinessType.UPDATE)
    public Result<Void> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        userService.updateProfile(dto);
        return Result.success();
    }

    @PutMapping("/password")
    @Log(title = "修改密码", businessType = BusinessType.UPDATE)
    public Result<Void> changePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        userService.changePassword(dto);
        return Result.success();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<List<SysUser>> list() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<SysUser> get(@PathVariable("id") Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<List<Long>> roles(@PathVariable("id") Long id) {
        return Result.success(userService.getRoleIds(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    public Result<Void> add(@Valid @RequestBody UserSaveDTO dto) {
        userService.saveUser(dto);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@Valid @RequestBody UserSaveDTO dto) {
        userService.updateUser(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    public Result<Void> delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
