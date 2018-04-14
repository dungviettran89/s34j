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
    private final NioConfiguration config;

    NioStore(Path baseDir, NioConfiguration config) {
        logger.info("NioStore(): baseDir=" + baseDir);
        Preconditions.checkNotNull(baseDir);
        this.baseDir = baseDir;
        this.config = config;
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
    public long save(String key, InputStream is) {
        logger.info("save(): key=" + key);
        StoreHelper.validateKey(key);
        try (OutputStream os = Files.newOutputStream(baseDir.resolve(key))) {
            long length = ByteStreams.copy(is, os);
            logger.info("save(): length=" + length);
            return length;
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
        } catch (Exception deleteException) {
            logger.warn("delete(): deleteException=" + deleteException, deleteException);
            return false;
        }
    }

    @Override
    public long getAvailableBytes(long usedByte) {
        if (config != null && config.containsKey("totalBytes")) {
            //overrides using total bytes if need, in case of S3 nio
            long totalBytes = Long.parseLong(config.get("totalBytes"));
            return totalBytes > usedByte ? totalBytes - usedByte : 0;
        }

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
