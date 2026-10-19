package com.example.framework.file.manager;

import com.example.framework.file.config.FileProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class FileLifecycleCoordinatorTest {
    @TempDir Path directory;

    private FileLifecycleCoordinator coordinator() {
        FileProperties properties = new FileProperties();
        properties.setBasePath(directory.toString());
        return new FileLifecycleCoordinator(properties);
    }

    @Test
    void cleanupSkipsActiveUploadAcrossThreadsAndCanAcquireAfterCompletion() throws Exception {
        var coordinator = coordinator();
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try {
            try (var upload = coordinator.lockUpload("avatar/test.png")) {
                assertNull(worker.submit(() -> coordinator.tryLockFile("avatar/test.png")).get(3, TimeUnit.SECONDS));
            }
            try (var cleanup = coordinator.tryLockFile("avatar/test.png")) { assertNotNull(cleanup); }
        } finally { worker.shutdownNow(); }
    }

    @Test
    void sharedDirectoryLockAlsoProtectsSeparateCoordinatorInstances() {
        var first = coordinator();
        var second = coordinator();
        try (var upload = first.lockUpload("file.txt")) {
            assertNull(second.tryLockFile("file.txt"));
        }
        try (var cleanup = second.tryLockFile("file.txt")) { assertNotNull(cleanup); }
    }

    @Test
    void cleanupTasksAreMutuallyExclusive() {
        var first = coordinator();
        var second = coordinator();
        try (var task = first.tryLockCleanup()) {
            assertNotNull(task);
            assertNull(second.tryLockCleanup());
        }
    }

    @Test
    void batchCanHoldCollidingStripesWithoutOverlappingLockFailure() {
        var coordinator = coordinator();
        try (var first = coordinator.lockUpload("same"); var nested = coordinator.lockUpload("same")) {
            assertNotNull(nested);
            assertNull(coordinator.tryLockFile("same"));
        }
    }
}
