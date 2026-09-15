package com.example.system.controller;

import com.example.common.core.Result;
import com.example.system.domain.dto.MenuSaveDTO;
import com.example.system.domain.entity.SysMenu;
import com.example.system.domain.vo.MenuTreeVO;
import com.example.system.service.SysMenuService;
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
@RequestMapping("/system/menu")
public class SysMenuController {

    private final SysMenuService menuService;

    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> currentUserMenuTree() {
        return Result.success(menuService.getCurrentUserMenuTree());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<SysMenu>> list() {
        return Result.success(menuService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<SysMenu> get(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<Void> add(@Valid @RequestBody MenuSaveDTO dto) {
        menuService.saveMenu(dto);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> edit(@Valid @RequestBody MenuSaveDTO dto) {
        menuService.updateMenu(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
}
