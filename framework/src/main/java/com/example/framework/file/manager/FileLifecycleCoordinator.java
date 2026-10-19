package com.example.framework.file.manager;

import com.example.common.exception.BusinessException;
import com.example.framework.file.config.FileProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReentrantLock;

/** Fixed lock stripes coordinate uploads/cleanup across threads and processes sharing a directory. */
@Component
public class FileLifecycleCoordinator {
    private final Path lockDirectory;
    private final ReentrantLock[] locks = new ReentrantLock[258];

    public FileLifecycleCoordinator(FileProperties properties) {
        lockDirectory = Path.of(properties.getBasePath()).toAbsolutePath().normalize().resolve(".locks");
        for (int i = 0; i < locks.length; i++) locks[i] = new ReentrantLock();
    }

    public Lease lockUpload(String relativePath) {
        return acquire(Math.floorMod(relativePath.hashCode(), 256), false);
    }

    public Lease tryLockFile(String relativePath) {
        return acquire(Math.floorMod(relativePath.hashCode(), 256), true);
    }

    public Lease tryLockCleanup() {
        return acquire(256, true);
    }

    public Lease lockBatchUpload() {
        return acquire(257, false);
    }

    private Lease acquire(int stripe, boolean nonBlocking) {
        ReentrantLock local = locks[stripe];
        if (nonBlocking && local.isHeldByCurrentThread()) return null;
        if (nonBlocking) {
            if (!local.tryLock()) return null;
        } else {
            local.lock();
        }
        if (local.getHoldCount() > 1) return new Lease(local, null, null);
        FileChannel channel = null;
        try {
            Files.createDirectories(lockDirectory);
            channel = FileChannel.open(lockDirectory.resolve(stripe + ".lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock fileLock = nonBlocking ? channel.tryLock() : channel.lock();
            if (fileLock == null) {
                channel.close();
                local.unlock();
                return null;
            }
            return new Lease(local, channel, fileLock);
        } catch (OverlappingFileLockException ex) {
            try { if (channel != null) channel.close(); } catch (IOException ignored) { }
            local.unlock();
            if (nonBlocking) return null;
            throw new BusinessException("文件操作正在执行");
        } catch (IOException | RuntimeException ex) {
            if (channel != null) {
                try { channel.close(); } catch (IOException ignored) { }
            }
            local.unlock();
            throw new BusinessException("无法锁定文件生命周期操作");
        }
    }

    public static final class Lease implements AutoCloseable {
        private final ReentrantLock local;
        private final FileChannel channel;
        private final FileLock fileLock;
        private boolean closed;

        private Lease(ReentrantLock local, FileChannel channel, FileLock fileLock) {
            this.local = local;
            this.channel = channel;
            this.fileLock = fileLock;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            try {
                if (fileLock != null) fileLock.release();
            } catch (IOException ignored) {
                // Closing the channel also releases the OS lock.
            } finally {
                try { if (channel != null) channel.close(); } catch (IOException ignored) { }
                local.unlock();
            }
        }
    }
}
