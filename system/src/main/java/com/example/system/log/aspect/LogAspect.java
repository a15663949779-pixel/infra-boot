package com.example.system.log.aspect;

import com.example.common.annotation.Log;
import com.example.common.utils.IpUtils;
import com.example.common.utils.SecurityUtils;
import com.example.system.domain.entity.SysOperLog;
import com.example.system.log.event.OperLogEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Around("@annotation(controllerLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, Log controllerLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperLog operLog = new SysOperLog();
        operLog.setOperTime(LocalDateTime.now());
        operLog.setStatus(1);

        HttpServletRequest request = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            request = attributes.getRequest();
        }

        if (request != null) {
            operLog.setRequestMethod(request.getMethod());
            operLog.setOperUrl(request.getRequestURI());
            operLog.setOperIp(IpUtils.getIpAddr(request));
        }

        try {
            operLog.setOperUserId(SecurityUtils.getUserId());
            operLog.setOperName(SecurityUtils.getUsername());
        } catch (Exception ignored) {
            operLog.setOperName("匿名/未登录");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        operLog.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName() + "()");
        operLog.setTitle(controllerLog.title());
        operLog.setBusinessType(controllerLog.businessType().name());

        if (controllerLog.isSaveRequestData()) {
            setRequestParams(joinPoint, operLog);
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            if (controllerLog.isSaveResponseData() && result != null) {
                String resultJson = objectMapper.writeValueAsString(result);
                operLog.setJsonResult(truncate(resultJson, 2000));
            }
        } catch (Throwable e) {
            operLog.setStatus(0);
            operLog.setErrorMsg(truncate(e.getMessage() != null ? e.getMessage() : e.toString(), 2000));
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            operLog.setCostTime(costTime);
            eventPublisher.publishEvent(new OperLogEvent(operLog));
        }

        return result;
    }

    private void setRequestParams(ProceedingJoinPoint joinPoint, SysOperLog operLog) {
        Object[] args = joinPoint.getArgs();
        List<Object> validArgs = new ArrayList<>();
        for (Object arg : args) {
            if (isFilterObject(arg)) {
                continue;
            }
            validArgs.add(arg);
        }
        try {
            String params = objectMapper.writeValueAsString(validArgs);
            operLog.setOperParam(truncate(params, 2000));
        } catch (Exception e) {
            log.warn("序列化请求参数异常: {}", e.getMessage());
        }
    }

    private boolean isFilterObject(Object o) {
        if (o == null) return true;
        Class<?> clazz = o.getClass();
        return (clazz.isArray() && clazz.getComponentType().isAssignableFrom(MultipartFile.class))
                || MultipartFile.class.isAssignableFrom(clazz)
                || HttpServletRequest.class.isAssignableFrom(clazz)
                || HttpServletResponse.class.isAssignableFrom(clazz)
                || BindingResult.class.isAssignableFrom(clazz);
    }

    private String truncate(String val, int maxLen) {
        if (val == null) return null;
        return val.length() > maxLen ? val.substring(0, maxLen) + "...(截断)" : val;
    }
}
