package com.example.system.controller;

import com.example.common.security.LoginUser;
import com.example.framework.security.PermissionService;
import com.example.system.domain.dto.FileQueryDTO;
import com.example.system.service.FileCleanupService;
import com.example.system.service.SysFileService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SysFileControllerSecurityTest {
    private AnnotationConfigApplicationContext context;
    private SysFileController controller;

    @BeforeEach void setup() {
        context = new AnnotationConfigApplicationContext(Config.class);
        controller = context.getBean(SysFileController.class);
        login(42L, Set.of());
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); context.close(); }

    @Test void ordinaryAccountCannotRunGlobalQueriesOrMaintenance() {
        assertThrows(AccessDeniedException.class, () -> controller.list(new FileQueryDTO()));
        assertThrows(AccessDeniedException.class, controller::manualCleanupTimeout);
        assertThrows(AccessDeniedException.class, controller::manualCleanupOrphan);
        assertThrows(AccessDeniedException.class, () -> controller.cleanupLogs(null));
        verifyNoInteractions(context.getBean(FileCleanupService.class), context.getBean(SysFileService.class));
    }
    @Test void avatarExceptionDoesNotAllowGeneralOrBatchUploads() {
        var file = new MockMultipartFile("file", new byte[] {1});
        assertDoesNotThrow(() -> controller.upload(file, "avatar"));
        assertThrows(AccessDeniedException.class, () -> controller.upload(file, "document"));
        assertThrows(AccessDeniedException.class, () -> controller.uploadBatch(java.util.List.of(file), "avatar"));
    }
    @Test void grantingListDoesNotImplicitlyGrantMaintenance() {
        login(42L, Set.of("system:file:list"));
        assertDoesNotThrow(() -> controller.list(new FileQueryDTO()));
        assertThrows(AccessDeniedException.class, controller::manualCleanupOrphan);
    }
    @Test void cleanupPermissionAllowsMaintenance() {
        login(42L, Set.of("system:file:cleanup"));
        controller.manualCleanupOrphan();
        verify(context.getBean(FileCleanupService.class)).cleanupOrphanFiles("MANUAL");
    }
    @Test void superAdminCanRunMaintenanceWithoutExplicitMenuPermission() {
        login(1L, Set.of());
        assertDoesNotThrow(controller::manualCleanupTimeout);
    }

    private void login(Long id, Set<String> permissions) {
        LoginUser user = LoginUser.builder().userId(id).permissions(permissions).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean("ss") PermissionService permissions() { return new PermissionService(); }
        @Bean SysFileService files() { return mock(SysFileService.class); }
        @Bean FileCleanupService cleanup() { return mock(FileCleanupService.class); }
        @Bean SysFileController controller(SysFileService files, FileCleanupService cleanup) { return new SysFileController(files, cleanup); }
    }
}
