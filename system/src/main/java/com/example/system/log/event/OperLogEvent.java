package com.example.system.log.event;

import com.example.system.domain.entity.SysOperLog;
import org.springframework.context.ApplicationEvent;

public class OperLogEvent extends ApplicationEvent {

    public OperLogEvent(SysOperLog operLog) {
        super(operLog);
    }

    public SysOperLog getOperLog() {
        return (SysOperLog) getSource();
    }
}
