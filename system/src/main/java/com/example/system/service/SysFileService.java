package com.example.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.domain.dto.FileQueryDTO;
import com.example.system.domain.entity.SysFile;
import com.example.system.domain.vo.FileUploadVO;
import com.example.system.domain.vo.SysFileVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public interface SysFileService extends IService<SysFile> {

    /**
     * 单文件上传
     */
    FileUploadVO upload(MultipartFile file, String businessType);

    /**
     * 批量文件上传
     */
    List<FileUploadVO> uploadBatch(List<MultipartFile> files, String businessType);

    /**
     * 文件下载（支持多机回源）
     */
    void download(Long fileId, HttpServletResponse response);

    /**
     * 批量/单个删除
     */
    void deleteByIds(List<Long> fileIds);

    /**
     * 分页查询文件列表
     */
    Page<SysFileVO> list(FileQueryDTO queryDTO);

    /**
     * 获取文件详情
     */
    SysFileVO getDetail(Long fileId);
}
