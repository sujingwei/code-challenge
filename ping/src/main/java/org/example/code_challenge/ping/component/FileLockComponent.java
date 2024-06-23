package org.example.code_challenge.ping.component;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class FileLockComponent {
    private final static Logger log = LoggerFactory.getLogger(FileLockComponent.class);

    // file 0
    private final static File LOCK_FILE_0 = new File(FileUtils.getTempDirectory(), "fileLock_0.lock");
    // file 1
    private final static File LOCK_FILE_1 = new File(FileUtils.getTempDirectory(), "fileLock_1.lock");


    private RandomAccessFile f0 = null, f1 = null;
    private FileChannel c0 = null, c1 = null;

    @PostConstruct
    public void init() throws FileNotFoundException {
        f0 = new RandomAccessFile(LOCK_FILE_0, "rw");
        c0 = f0.getChannel();
        f1 = new RandomAccessFile(LOCK_FILE_1, "rw");
        c1 = f1.getChannel();
    }

    @PreDestroy
    public void destroy() throws IOException {
        if (f0 != null)
            f0.close();
        if (c0 != null)
            c0.close();
        if (f1 != null)
            f1.close();
        if (c1 != null)
            c1.close();
    }


    /**
     * 获取FileLock
     *
     * @return
     */
    public synchronized FileLock getLock() {
        log.info("Get file lock....");
        try {
            return c0.lock();
        } catch (IOException | OverlappingFileLockException e) {
            try {
                return c1.lock();
            } catch (IOException | OverlappingFileLockException ex) {
                return null;
            }
        }
    }

    /**
     * 释放FileLock
     *
     * @param lock
     */
    public void releaseLock(FileLock lock) {
        if (lock == null) return;
        ScheduledExecutorService e = Executors.newSingleThreadScheduledExecutor();
        e.schedule(() -> {
            try {
                if (lock.isValid()) {
                    lock.release();
                    log.info("Release file Lock.");
                }
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }, 1, TimeUnit.SECONDS);
        e.shutdown();
    }

}
