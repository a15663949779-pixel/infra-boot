package com.example.system.service.impl;

import com.example.common.security.LoginUser;
import com.example.common.exception.BusinessException;
import com.example.framework.file.client.RemoteStorageClient;
import com.example.framework.file.config.FileProperties;
import com.example.framework.file.manager.*;
import com.example.framework.security.PermissionService;
import com.example.system.domain.dto.FileQueryDTO;
import com.example.system.domain.entity.SysFile;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.*;

import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SysFileSecurityTest {
    @TempDir Path directory;
    private SysFileServiceImpl service;
    private FileLifecycleCoordinator lifecycle;
    private SysFile saved;

    @BeforeEach
    void setup() {
        FileProperties properties = new FileProperties();
        properties.setBasePath(directory.toString());
        lifecycle = new FileLifecycleCoordinator(properties);
        service = spy(new SysFileServiceImpl(new FileStorageManager(properties, mock(RemoteStorageClient.class)),
                lifecycle, new PermissionService()));
        login(42L, Set.of());
        doAnswer(i -> { saved = i.getArgument(0); saved.setFileId(9L); return true; }).when(service).save(any(SysFile.class));
        doReturn(true).when(service).updateById(any(SysFile.class));
    }

    @AfterEach
    void clear() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) complete(TransactionSynchronization.STATUS_ROLLED_BACK);
        SecurityContextHolder.clearContext();
    }

    @Test void ordinaryUserCannotListAllFilesOrUploadArbitraryAttachments() {
        assertThrows(AccessDeniedException.class, () -> service.list(new FileQueryDTO()));
        assertThrows(AccessDeniedException.class, () -> service.upload(new MockMultipartFile("file", "a.txt", "text/plain", new byte[] {1}), "document"));
    }
    @Test void ordinaryUserCannotReadOrDeleteAnotherUsersFiles() {
        SysFile other = file(77L);
        doReturn(other).when(service).getById(9L);
        doReturn(List.of(other)).when(service).listByIds(anyCollection());
        assertThrows(AccessDeniedException.class, () -> service.getDetail(9L));
        assertThrows(AccessDeniedException.class, () -> service.download(9L, new MockHttpServletResponse()));
        assertThrows(AccessDeniedException.class, () -> service.deleteByIds(List.of(9L)));
        verify(service, never()).removeBatchByIds(anyCollection());
    }
    @Test void batchDeletionChecksEveryFileBeforeMutatingAnything() {
        doReturn(List.of(file(42L), file(77L))).when(service).listByIds(anyCollection());
        assertThrows(AccessDeniedException.class, () -> service.deleteByIds(List.of(9L, 10L)));
        verify(service, never()).removeBatchByIds(anyCollection());
    }
    @Test void unknownHistoricalOwnershipIsNotTreatedAsPublic() {
        doReturn(file(null)).when(service).getById(9L);
        assertThrows(AccessDeniedException.class, () -> service.getDetail(9L));
        login(1L, Set.of());
        assertNotNull(service.getDetail(9L));
    }
    @Test void ownerCanReadOwnFileAndManagerNeedsSpecificPermission() {
        doReturn(file(42L)).when(service).getById(9L);
        assertNotNull(service.getDetail(9L));
        doReturn(file(77L)).when(service).getById(9L);
        login(42L, Set.of("system:file:query"));
        assertNotNull(service.getDetail(9L));
        assertThrows(AccessDeniedException.class, () -> service.download(9L, new MockHttpServletResponse()));
    }
    @Test void avatarBindingRequiresOwnershipTypeAndCompletedUpload() {
        SysFile avatar = file(42L);
        doReturn(avatar).when(service).getById(9L);
        assertDoesNotThrow(() -> service.validateAvatar(9L));
        avatar.setOwnerId(77L);
        assertThrows(AccessDeniedException.class, () -> service.validateAvatar(9L));
        avatar.setOwnerId(42L); avatar.setBusinessType("document");
        assertThrows(AccessDeniedException.class, () -> service.validateAvatar(9L));
        avatar.setBusinessType("avatar"); avatar.setUploadStatus(0);
        assertThrows(AccessDeniedException.class, () -> service.validateAvatar(9L));
    }
    @Test void fakeImageCannotUseAvatarUploadException() {
        assertThrows(BusinessException.class, () -> service.upload(new MockMultipartFile("file", "fake.png", "image/png", "<script>alert(1)</script>".getBytes()), "avatar"));
        verify(service, never()).save(any(SysFile.class));
    }
    @Test void pathTraversalBusinessTypeIsRejected() {
        login(1L, Set.of());
        assertThrows(BusinessException.class, () -> service.upload(image(), "../avatar"));
    }
    @Test void avatarUploadStoresOwnerAndBusinessType() {
        service.upload(image(), "avatar");
        assertEquals(42L, saved.getOwnerId());
        assertEquals("avatar", saved.getBusinessType());
        assertTrue(Files.exists(directory.resolve(saved.getFilePath())));
    }
    @Test void cleanupCannotAcquireFileBetweenUploadReturnAndDatabaseCommit() {
        TransactionSynchronizationManager.initSynchronization();
        service.upload(image(), "avatar");
        assertNull(lifecycle.tryLockFile(saved.getFilePath()));
        complete(TransactionSynchronization.STATUS_COMMITTED);
        try (var lease = lifecycle.tryLockFile(saved.getFilePath())) { assertNotNull(lease); }
        assertTrue(Files.exists(directory.resolve(saved.getFilePath())));
    }
    @Test void transactionRollbackRemovesUploadedFileAndReleasesLock() {
        TransactionSynchronizationManager.initSynchronization();
        service.upload(image(), "avatar");
        complete(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertFalse(Files.exists(directory.resolve(saved.getFilePath())));
        try (var lease = lifecycle.tryLockFile(saved.getFilePath())) { assertNotNull(lease); }
    }
    @Test void physicalDeletionWaitsForCommitAndDoesNotRunAfterRollback() throws Exception {
        SysFile own = file(42L);
        Path local = directory.resolve(own.getFilePath());
        Files.createDirectories(local.getParent()); Files.writeString(local, "image");
        doReturn(List.of(own)).when(service).listByIds(anyCollection());
        doReturn(true).when(service).removeBatchByIds(anyCollection());
        TransactionSynchronizationManager.initSynchronization();
        service.deleteByIds(List.of(9L));
        assertTrue(Files.exists(local));
        complete(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertTrue(Files.exists(local));
        TransactionSynchronizationManager.initSynchronization();
        service.deleteByIds(List.of(9L));
        complete(TransactionSynchronization.STATUS_COMMITTED);
        assertFalse(Files.exists(local));
    }

    private MockMultipartFile image() {
        byte[] data = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aK1cAAAAASUVORK5CYII=");
        return new MockMultipartFile("file", "avatar.png", "image/png", data);
    }
    private SysFile file(Long owner) {
        SysFile file = new SysFile(); file.setFileId(9L); file.setOwnerId(owner);
        file.setDeleted(0); file.setFilePath("avatar/test.png"); file.setBusinessType("avatar"); file.setUploadStatus(1);
        return file;
    }
    private void login(Long id, Set<String> permissions) {
        LoginUser user = LoginUser.builder().userId(id).permissions(permissions).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
    private void complete(int status) {
        var synchronizations = TransactionSynchronizationManager.getSynchronizations();
        try {
            if (status == TransactionSynchronization.STATUS_COMMITTED) synchronizations.forEach(TransactionSynchronization::afterCommit);
            synchronizations.forEach(s -> s.afterCompletion(status));
        } finally { TransactionSynchronizationManager.clearSynchronization(); }
    }
}
