package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.system.domain.entity.SysOperLog;
import com.example.system.mapper.SysOperLogMapper;
import com.example.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog>
        implements SysOperLogService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void addLog(SysOperLog operLog) {
        save(operLog);
    }

    @Override
    public Page<SysOperLog> listLogs(String title, String operName, String businessType,
                                     Integer status, String beginTime, String endTime,
                                     Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(title), SysOperLog::getTitle, title)
                .like(StringUtils.hasText(operName), SysOperLog::getOperName, operName)
                .eq(StringUtils.hasText(businessType), SysOperLog::getBusinessType, businessType)
                .eq(status != null, SysOperLog::getStatus, status);

        if (StringUtils.hasText(beginTime)) {
            LocalDateTime start = LocalDateTime.of(LocalDate.parse(beginTime, DATE_FMT), LocalTime.MIN);
            wrapper.ge(SysOperLog::getOperTime, start);
        }
        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDateTime.of(LocalDate.parse(endTime, DATE_FMT), LocalTime.MAX);
            wrapper.le(SysOperLog::getOperTime, end);
        }

        wrapper.orderByDesc(SysOperLog::getOperTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void deleteLogByIds(Long[] ids) {
        if (ids != null && ids.length > 0) {
            removeByIds(Arrays.asList(ids));
        }
    }

    @Override
    public void cleanAllLogs() {
        remove(new LambdaQueryWrapper<>());
    }
}
