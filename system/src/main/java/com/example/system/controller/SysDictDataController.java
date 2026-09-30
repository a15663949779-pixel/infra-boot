package com.example.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.Log;
import com.example.common.core.Result;
import com.example.common.enums.BusinessType;
import com.example.system.domain.entity.SysDictData;
import com.example.system.service.SysDictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final SysDictDataService dictDataService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public Result<IPage<SysDictData>> list(
            Page<SysDictData> page,
            @RequestParam String dictType,
            @RequestParam(required = false) String dictLabel,
            @RequestParam(required = false) Integer status) {
        return Result.success(dictDataService.listPage(page, dictType, dictLabel, status));
    }

    @GetMapping("/type/{dictType}")
    public Result<List<SysDictData>> getByType(@PathVariable String dictType) {
        return Result.success(dictDataService.selectDictDataByType(dictType));
    }

    @GetMapping("/{dictCode}")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public Result<SysDictData> detail(@PathVariable Long dictCode) {
        return Result.success(dictDataService.getById(dictCode));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody SysDictData dictData) {
        dictDataService.save(dictData);
        dictDataService.clearDictCache(dictData.getDictType());
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody SysDictData dictData) {
        dictDataService.updateById(dictData);
        dictDataService.clearDictCache(dictData.getDictType());
        return Result.success();
    }

    @DeleteMapping("/{dictCodes}")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable("dictCodes") Long[] dictCodes) {
        for (Long code : dictCodes) {
            SysDictData data = dictDataService.getById(code);
            if (data != null) {
                dictDataService.clearDictCache(data.getDictType());
            }
        }
        dictDataService.removeByIds(java.util.Arrays.asList(dictCodes));
        return Result.success();
    }
}
