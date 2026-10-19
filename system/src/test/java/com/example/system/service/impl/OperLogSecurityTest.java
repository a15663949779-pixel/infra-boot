package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.system.domain.entity.SysOperLog;
import com.example.system.log.AuditSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OperLogSecurityTest {
    @Test
    void historicalPasswordsDoNotReachLogListResponses() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), SysOperLog.class);
        SysOperLogServiceImpl service = spy(new SysOperLogServiceImpl(new AuditSanitizer(new ObjectMapper())));
        SysOperLog record = new SysOperLog();
        record.setOperParam("[{\"oldPassword\":\"old-secret\",\"newPassword\":\"new-secret\"}]");
        record.setJsonResult("{\"accessToken\":\"legacy-token");
        Page<SysOperLog> page = new Page<>(); page.setRecords(List.of(record));
        doReturn(page).when(service).page(any(Page.class), any());
        SysOperLog result = service.listLogs(null, null, null, null, null, null, 1, 10).getRecords().get(0);
        assertFalse(result.getOperParam().contains("old-secret"));
        assertFalse(result.getOperParam().contains("new-secret"));
        assertFalse(result.getJsonResult().contains("legacy-token"));
    }

    @Test
    void persistenceAlsoRedactsCallersOutsideTheAspect() {
        SysOperLogServiceImpl service = spy(new SysOperLogServiceImpl(new AuditSanitizer(new ObjectMapper())));
        doReturn(true).when(service).save(any(SysOperLog.class));
        SysOperLog record = new SysOperLog();
        record.setOperParam("{\"password\":\"raw-secret\"}");
        service.addLog(record);
        assertFalse(record.getOperParam().contains("raw-secret"));
    }
}
