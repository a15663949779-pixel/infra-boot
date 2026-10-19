package com.example.system.log;

import com.example.common.annotation.Log;
import com.example.system.domain.dto.PasswordUpdateDTO;
import com.example.system.domain.dto.UserSaveDTO;
import com.example.system.log.aspect.LogAspect;
import com.example.system.log.event.OperLogEvent;
import com.example.system.controller.SysUserController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditSanitizerTest {
    private final AuditSanitizer sanitizer = new AuditSanitizer(new ObjectMapper());

    @Test
    void redactsNestedSecretsInMapsAndArraysWithoutChangingSource() throws Exception {
        Map<String, Object> value = Map.of("username", "alice", "nested", List.of(
                Map.of("oldPassword", "old-value", "NEW_PASSWORD", "new-value", "access_token", "token-value"),
                Map.of("api-key", "key-value", "password", "create-value")));
        String json = sanitizer.toJson(value);
        for (String secret : List.of("old-value", "new-value", "token-value", "key-value", "create-value")) {
            assertFalse(json.contains(secret));
        }
        assertTrue(json.contains("alice"));
        assertTrue(value.toString().contains("old-value"));
    }

    @Test
    void passwordAndUserDtosAreRedacted() throws Exception {
        PasswordUpdateDTO password = new PasswordUpdateDTO();
        password.setOldPassword("original-password");
        password.setNewPassword("replacement-password");
        UserSaveDTO user = new UserSaveDTO();
        user.setUsername("alice");
        user.setPassword("initial-password");
        String json = sanitizer.toJson(List.of(password, user));
        assertFalse(json.contains("original-password"));
        assertFalse(json.contains("replacement-password"));
        assertFalse(json.contains("initial-password"));
        assertEquals("initial-password", user.getPassword());
    }

    @Test
    void aspectPublishesOnlySanitizedRequestAndResponse() throws Throwable {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        LogAspect aspect = new LogAspect(publisher, sanitizer);
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        var method = Fixture.class.getMethod("save");
        when(point.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(point.getTarget()).thenReturn(new Fixture());
        when(point.getArgs()).thenReturn(new Object[] { Map.of("password", "request-secret") });
        Map<String, String> response = Map.of("accessToken", "response-secret");
        when(point.proceed()).thenReturn(response);
        assertSame(response, aspect.doAround(point, method.getAnnotation(Log.class)));
        ArgumentCaptor<OperLogEvent> event = ArgumentCaptor.forClass(OperLogEvent.class);
        verify(publisher).publishEvent(event.capture());
        assertFalse(event.getValue().getOperLog().getOperParam().contains("request-secret"));
        assertFalse(event.getValue().getOperLog().getJsonResult().contains("response-secret"));
    }

    @Test
    void passwordEndpointDoesNotCollectRequestPayloadAtAll() throws Exception {
        Log log = SysUserController.class.getMethod("changePassword", PasswordUpdateDTO.class).getAnnotation(Log.class);
        assertFalse(log.isSaveRequestData());
    }

    @Test
    void existingLogsAreRedactedAndTruncatedLegacyPayloadIsHidden() {
        assertFalse(sanitizer.sanitizeStoredJson("{\"password\":\"legacy-secret\",\"username\":\"alice\"}").contains("legacy-secret"));
        assertFalse(sanitizer.sanitizeStoredJson("{\"password\":\"truncated-secret").contains("truncated-secret"));
        assertNull(sanitizer.sanitizeStoredJson(null));
    }

    public static class Fixture {
        @Log public void save() { }
    }
}
