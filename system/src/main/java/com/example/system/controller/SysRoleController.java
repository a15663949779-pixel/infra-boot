package com.example.system.controller;

import com.example.common.annotation.Log;
import com.example.common.core.Result;
import com.example.common.enums.BusinessType;
import com.example.system.domain.dto.RoleSaveDTO;
import com.example.system.domain.entity.SysRole;
import com.example.system.service.SysRoleService;
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
@RequestMapping("/system/role")
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<SysRole> get(@PathVariable("id") Long id) {
        return Result.success(roleService.getById(id));
    }

    @GetMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<List<Long>> menus(@PathVariable("id") Long id) {
        return Result.success(roleService.getMenuIds(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    public Result<Void> add(@Valid @RequestBody RoleSaveDTO dto) {
        roleService.saveRole(dto);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@Valid @RequestBody RoleSaveDTO dto) {
        roleService.updateRole(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    public Result<Void> delete(@PathVariable("id") Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
}
