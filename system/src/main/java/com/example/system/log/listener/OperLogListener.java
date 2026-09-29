package com.example.system.log.listener;

import com.example.system.log.event.OperLogEvent;
import com.example.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogListener {

    private final SysOperLogService operLogService;

    @Async("logThreadPoolExecutor")
    @EventListener
    public void recordLog(OperLogEvent event) {
        try {
            operLogService.addLog(event.getOperLog());
        } catch (Exception e) {
            log.error("异步保存操作日志失败: {}", e.getMessage(), e);
        }
    }
}
