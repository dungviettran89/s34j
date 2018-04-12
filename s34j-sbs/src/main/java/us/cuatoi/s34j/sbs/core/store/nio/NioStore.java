package us.cuatoi.s34j.sbs.core.store.nio;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
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
    public void load(String key, FileConsumer consumer) {
        logger.info("load(): key=" + key);
        StoreHelper.validateKey(key);
        try (InputStream is = Files.newInputStream(baseDir.resolve(key))) {
            consumer.accept(is);
        } catch (IOException ex) {
            logger.error("load(): ex=" + ex, ex);
            throw new StoreException(ex);
        }
    }

    @Override
    public void save(String key, InputStream stream) {
        logger.info("save(): key=" + key);
        StoreHelper.validateKey(key);
        try (OutputStream os = Files.newOutputStream(baseDir.resolve(key))) {
            long total = ByteStreams.copy(stream, os);
            logger.info("save(): total=" + total);
        } catch (IOException ex) {
            logger.error("save(): ex=" + ex, ex);
            throw new StoreException(ex);
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
    public long getTotal() {
        try {
            long totalSpace = Files.getFileStore(baseDir).getTotalSpace();
            logger.info("getTotal(): totalSpace=" + totalSpace);
            return totalSpace;
        } catch (IOException ex) {
            logger.error("getTotal(): ex=" + ex, ex);
            throw new StoreException(ex);
        }
    }

    @Override
    public long getUsed() {
        long used = getTotal() - getAvailable();
        logger.info("getUsed(): used=" + used);
        return used;
    }

    @Override
    public long getAvailable() {
        try {
            long usableSpace = Files.getFileStore(baseDir).getUsableSpace();
            logger.info("getAvailable(): usableSpace=" + usableSpace);
            return usableSpace;
        } catch (IOException ex) {
            logger.error("getAvailable(): ex=" + ex, ex);
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
