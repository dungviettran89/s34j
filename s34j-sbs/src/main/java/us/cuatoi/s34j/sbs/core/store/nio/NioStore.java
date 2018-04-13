package us.cuatoi.s34j.sbs.core.store.nio;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.sbs.core.StoreHelper;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class NioStore implements Store {

    private static final Logger logger = LoggerFactory.getLogger(NioStore.class);
    private final Path baseDir;

    NioStore(Path baseDir) {
        logger.info("NioStore(): baseDir=" + baseDir);
        Preconditions.checkNotNull(baseDir);
        Preconditions.checkArgument(Files.exists(baseDir));
        Preconditions.checkArgument(Files.isDirectory(baseDir));
        this.baseDir = baseDir;
    }


    @Override
    public boolean has(String key) {
        logger.info("has(): key=" + key);
        StoreHelper.validateKey(key);
        boolean exists = Files.exists(baseDir.resolve(key));
        logger.info("has(): exists=" + exists);
        return exists;
    }

    @Override
    public long size(String key) {
        logger.info("size(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            long size = Files.size(baseDir.resolve(key));
            logger.info("size(): size=" + size);
            return size;
        } catch (IOException ex) {
            logger.error("size(): ex=" + ex, ex);
            throw new StoreException(ex);
        }
    }

    @Override
    public InputStream load(String key) {
        logger.info("load(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            return Files.newInputStream(baseDir.resolve(key));
        } catch (IOException openException) {
            logger.error("load(): openException=" + openException);
            throw new StoreException(openException);
        }
    }

    @Override
    public OutputStream save(String key) {
        logger.info("save(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            return Files.newOutputStream(baseDir.resolve(key));
        } catch (IOException openException) {
            logger.error("save(): openException=" + openException);
            throw new StoreException(openException);
        }
    }

    @Override
    public boolean delete(String key) {
        logger.info("delete(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            return Files.deleteIfExists(baseDir.resolve(key));
        } catch (IOException ex) {
            logger.error("save(): ex=" + ex, ex);
            throw new StoreException(ex);
        }
    }

    @Override
    public long getTotalBytes() {
        try {
            long totalSpace = Files.getFileStore(baseDir).getTotalSpace();
            logger.info("getTotalBytes(): totalSpace=" + totalSpace);
            return totalSpace;
        } catch (IOException ex) {
            logger.error("getTotalBytes(): ex=" + ex, ex);
            throw new StoreException(ex);
        }
    }

    @Override
    public long getUsedBytes() {
        long used = getTotalBytes() - getAvailableBytes();
        logger.info("getUsedBytes(): used=" + used);
        return used;
    }

    @Override
    public long getAvailableBytes() {
        try {
            long usableSpace = Files.getFileStore(baseDir).getUsableSpace();
            logger.info("getAvailableBytes(): usableSpace=" + usableSpace);
            return usableSpace;
        } catch (IOException ex) {
            logger.error("getAvailableBytes(): ex=" + ex, ex);
            throw new StoreException(ex);
        }

    }

    @Override
    public String toString() {
        return "NioStore{" +
                "baseDir=" + baseDir +
                '}';
    }
}