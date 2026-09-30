package com.example.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.Log;
import com.example.common.core.Result;
import com.example.common.enums.BusinessType;
import com.example.system.domain.entity.SysDictType;
import com.example.system.service.SysDictDataService;
import com.example.system.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final SysDictTypeService dictTypeService;
    private final SysDictDataService dictDataService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public Result<IPage<SysDictType>> list(
            Page<SysDictType> page,
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) Integer status) {
        return Result.success(dictTypeService.listPage(page, dictName, dictType, status));
    }

    @GetMapping("/{dictId}")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public Result<SysDictType> detail(@PathVariable Long dictId) {
        return Result.success(dictTypeService.getById(dictId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody SysDictType dictType) {
        dictTypeService.save(dictType);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody SysDictType dictType) {
        dictTypeService.updateById(dictType);
        return Result.success();
    }

    @DeleteMapping("/{dictIds}")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable("dictIds") Long[] dictIds) {
        dictTypeService.removeByIds(java.util.Arrays.asList(dictIds));
        return Result.success();
    }

    @DeleteMapping("/clearCache")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    public Result<Void> clearCache() {
        dictDataService.clearAllCache();
        return Result.success();
    }
}
