package com.example.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.Log;
import com.example.common.core.Result;
import com.example.common.enums.BusinessType;
import com.example.system.domain.entity.SysOperLog;
import com.example.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/operlog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService sysOperLogService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:operlog:list')")
    public Result<Page<SysOperLog>> list(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "operName", required = false) String operName,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SysOperLog> page = sysOperLogService.listLogs(title, operName, businessType,
                status, beginTime, endTime, pageNum, pageSize);
        return Result.success(page);
    }

    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAuthority('system:operlog:remove')")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable("ids") Long[] ids) {
        sysOperLogService.deleteLogByIds(ids);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @PreAuthorize("hasAuthority('system:operlog:remove')")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    public Result<Void> clean() {
        sysOperLogService.cleanAllLogs();
        return Result.success();
    }
}
