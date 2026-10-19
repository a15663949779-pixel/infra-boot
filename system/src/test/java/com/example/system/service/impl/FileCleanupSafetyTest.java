package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.example.framework.file.client.RemoteStorageClient;
import com.example.framework.file.config.FileProperties;
import com.example.framework.file.manager.*;
import com.example.framework.security.PermissionService;
import com.example.system.domain.entity.SysFile;
import com.example.system.mapper.*;
import com.example.system.service.SysFileService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.access.AccessDeniedException;

import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FileCleanupSafetyTest {
    @TempDir Path directory;
    private final SysFileMapper files = mock(SysFileMapper.class);
    private final SysFileService fileService = mock(SysFileService.class);
    private FileLifecycleCoordinator lifecycle;
    private FileCleanupServiceImpl cleanup;

    @BeforeEach void setup() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), SysFile.class);
        FileProperties properties = new FileProperties(); properties.setBasePath(directory.toString());
        lifecycle = new FileLifecycleCoordinator(properties);
        cleanup = new FileCleanupServiceImpl(fileService, files, mock(SysFileCleanupLogMapper.class),
                new FileStorageManager(properties, mock(RemoteStorageClient.class)), properties, lifecycle, new PermissionService());
        when(files.selectList(any())).thenReturn(List.of());
        when(files.selectCount(any())).thenReturn(0L);
    }

    @Test void recentUncommittedFilesAreNeverDeleted() throws Exception {
        Path file = diskFile("avatar/recent.png", false);
        cleanup.cleanupOrphanFiles("AUTO");
        assertTrue(Files.exists(file));
        verify(files, never()).selectCount(any());
    }
    @Test void oldActiveUploadIsProtectedEvenBeyondGracePeriod() throws Exception {
        Path file = diskFile("avatar/slow.png", true);
        try (var upload = lifecycle.lockUpload("avatar/slow.png")) { cleanup.cleanupOrphanFiles("AUTO"); }
        assertTrue(Files.exists(file));
    }
    @Test void referenceCommittedAfterInitialScanPreventsDeletion() throws Exception {
        Path file = diskFile("document/committed.txt", true);
        when(files.selectCount(any())).thenReturn(1L);
        cleanup.cleanupOrphanFiles("AUTO");
        assertTrue(Files.exists(file));
    }
    @Test void oldUnreferencedInactiveFileIsDeleted() throws Exception {
        Path file = diskFile("document/orphan.txt", true);
        cleanup.cleanupOrphanFiles("AUTO");
        assertFalse(Files.exists(file));
        assertTrue(Files.isDirectory(directory.resolve(".locks")));
    }
    @Test void timeoutCleanupRechecksFreshUploadStatus() throws Exception {
        Path path = diskFile("document/finished.txt", true);
        SysFile stale = record("document/finished.txt", 0);
        when(files.selectList(any())).thenReturn(List.of(stale));
        when(files.selectById(9L)).thenReturn(record(stale.getFilePath(), 1));
        cleanup.cleanupUploadingTimeout("AUTO");
        assertTrue(Files.exists(path));
        verify(fileService, never()).removeById(anyLong());
    }
    @Test void timeoutCleanupCannotDeleteAnActiveLongRunningUpload() throws Exception {
        Path path = diskFile("document/slow.txt", true);
        when(files.selectList(any())).thenReturn(List.of(record("document/slow.txt", 0)));
        try (var upload = lifecycle.lockUpload("document/slow.txt")) { cleanup.cleanupUploadingTimeout("AUTO"); }
        assertTrue(Files.exists(path));
        verify(files, never()).selectById(anyLong());
    }
    @Test void manualMaintenanceIsNotAvailableToAnonymousCallers() {
        assertThrows(AccessDeniedException.class, () -> cleanup.cleanupOrphanFiles("MANUAL"));
        assertThrows(AccessDeniedException.class, () -> cleanup.cleanupUploadingTimeout("MANUAL"));
    }

    private Path diskFile(String relative, boolean old) throws Exception {
        Path path = directory.resolve(relative); Files.createDirectories(path.getParent()); Files.writeString(path, "data");
        if (old) Files.setLastModifiedTime(path, FileTime.from(Instant.now().minusSeconds(7200)));
        return path;
    }
    private SysFile record(String path, int status) {
        SysFile file = new SysFile(); file.setFileId(9L); file.setFilePath(path); file.setUploadStatus(status);
        file.setCreateTime(LocalDateTime.now().minusHours(2)); return file;
    }
}
