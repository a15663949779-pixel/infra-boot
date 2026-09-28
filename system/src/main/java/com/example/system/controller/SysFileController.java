package com.example.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.Result;
import com.example.system.domain.dto.CleanupLogQueryDTO;
import com.example.system.domain.dto.FileQueryDTO;
import com.example.system.domain.vo.FileUploadVO;
import com.example.system.domain.vo.SysFileCleanupLogVO;
import com.example.system.domain.vo.SysFileVO;
import com.example.system.service.FileCleanupService;
import com.example.system.service.SysFileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/system/file")
public class SysFileController {

    private final SysFileService sysFileService;
    private final FileCleanupService fileCleanupService;

    public SysFileController(SysFileService sysFileService, FileCleanupService fileCleanupService) {
        this.sysFileService = sysFileService;
        this.fileCleanupService = fileCleanupService;
    }

    @PostMapping("/upload")
    public Result<FileUploadVO> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "businessType", defaultValue = "default") String businessType) {
        FileUploadVO vo = sysFileService.upload(file, businessType);
        return Result.success(vo);
    }

    @PostMapping("/upload/batch")
    public Result<List<FileUploadVO>> uploadBatch(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(value = "businessType", defaultValue = "default") String businessType) {
        List<FileUploadVO> voList = sysFileService.uploadBatch(files, businessType);
        return Result.success(voList);
    }

    @GetMapping("/download/{fileId}")
    public void download(
            @PathVariable("fileId") Long fileId,
            HttpServletResponse response) {
        sysFileService.download(fileId, response);
    }

    @DeleteMapping("/{fileIds}")
    public Result<Void> deleteByIds(@PathVariable("fileIds") List<Long> fileIds) {
        sysFileService.deleteByIds(fileIds);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<Page<SysFileVO>> list(FileQueryDTO queryDTO) {
        Page<SysFileVO> page = sysFileService.list(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/{fileId}")
    public Result<SysFileVO> getDetail(@PathVariable("fileId") Long fileId) {
        SysFileVO vo = sysFileService.getDetail(fileId);
        return Result.success(vo);
    }

    @PostMapping("/cleanup/timeout")
    public Result<Void> manualCleanupTimeout() {
        fileCleanupService.cleanupUploadingTimeout("MANUAL");
        return Result.success();
    }

    @PostMapping("/cleanup/orphan")
    public Result<Void> manualCleanupOrphan() {
        fileCleanupService.cleanupOrphanFiles("MANUAL");
        return Result.success();
    }

    @GetMapping("/cleanup/logs")
    public Result<Page<SysFileCleanupLogVO>> cleanupLogs(CleanupLogQueryDTO queryDTO) {
        Page<SysFileCleanupLogVO> page = fileCleanupService.listCleanupLogs(queryDTO);
        return Result.success(page);
    }
}
