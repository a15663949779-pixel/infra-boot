package com.example.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.domain.entity.SysOperLog;

public interface SysOperLogService extends IService<SysOperLog> {

    void addLog(SysOperLog operLog);

    Page<SysOperLog> listLogs(String title, String operName, String businessType,
                              Integer status, String beginTime, String endTime,
                              Integer pageNum, Integer pageSize);

    void deleteLogByIds(Long[] ids);

    void cleanAllLogs();
}
