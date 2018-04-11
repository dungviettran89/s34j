package us.cuatoi.s34j.sbs.core.store.nio;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;

public class NioStore implements Store {

    public static final Logger logger = LoggerFactory.getLogger(NioStore.class);
    private final Path baseDir;

    public NioStore(Path baseDir) {
        logger.info("NioStore(): baseDir=" + baseDir);
        Preconditions.checkNotNull(baseDir);
        Preconditions.checkArgument(Files.exists(baseDir));
        Preconditions.checkArgument(Files.isDirectory(baseDir));
        this.baseDir = baseDir;
    }

    @Override
    public boolean has(String key) {
        logger.info("has(): key=" + key);
        return Files.exists(getFile(key));
    }

    @Override
    public long size(String key) throws IOException {
        logger.info("size(): key=" + key);
        return Files.size(getFile(key));
    }

    @Override
    public void save(String key, InputStream is) throws IOException {
        logger.info("save(): key=" + key);
        Path file = getFile(key);
        logger.info("save(): is=" + is);
        Preconditions.checkNotNull(is);
        try (OutputStream os = Files.newOutputStream(file, CREATE)) {
            long count = ByteStreams.copy(is, os);
            logger.info("save(): count=" + count);
        }
    }

    private Path getFile(String key) {
        logger.debug("getFile(): key=" + key);
        Preconditions.checkArgument(StringUtils.isNotBlank(key));
        String normalizedKey = StoreHelper.normalizeKey(key);
        logger.debug("getFile(): normalizedKey=" + normalizedKey);
        return baseDir.resolve(normalizedKey);
    }

    @Override
    public InputStream load(String key) throws IOException {
        logger.info("load(): key=" + key);
        return Files.newInputStream(getFile(key));
    }

    @Override
    public boolean delete(String key) throws IOException {
        logger.info("delete(): key=" + key);
        return Files.deleteIfExists(getFile(key));
    }

    @Override
    public long getTotal() throws IOException {
        long totalSpace = Files.getFileStore(baseDir).getTotalSpace();
        logger.info("getTotal(): totalSpace=" + totalSpace);
        return totalSpace;
    }

    @Override
    public long getUsed() throws IOException {
        long used = getTotal() - getAvailable();
        logger.info("getUsed(): used=" + used);
        return used;
    }

    @Override
    public long getAvailable() throws IOException {
        long usableSpace = Files.getFileStore(baseDir).getUsableSpace();
        logger.info("getAvailable(): usableSpace=" + usableSpace);
        return usableSpace;

    }
}
