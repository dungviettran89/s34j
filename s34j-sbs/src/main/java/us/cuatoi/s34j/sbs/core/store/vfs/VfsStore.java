package us.cuatoi.s34j.sbs.core.store.vfs;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.sbs.core.StoreHelper;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * An Apache VFS backed store.
 */
public class VfsStore implements Store {

    private static final Logger logger = LoggerFactory.getLogger(VfsStore.class);
    private final FileObject file;
    private final long totalBytes;

    public VfsStore(FileObject folder, long totalBytes) throws FileSystemException {
        logger.info("VfsStore() folder=" + folder);
        Preconditions.checkNotNull(folder);
        Preconditions.checkArgument(folder.isFolder());
        Preconditions.checkArgument(totalBytes > 0);
        this.file = folder;
        this.totalBytes = totalBytes;
    }

    @Override
    public boolean has(String key) {
        logger.info("has(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            return file.resolveFile(key).exists();
        } catch (FileSystemException exception) {
            logger.warn("has(): exception=" + exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public long size(String key) {
        logger.info("size(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            return file.resolveFile(key).getContent().getSize();
        } catch (FileSystemException exception) {
            logger.warn("size(): exception=" + exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public InputStream load(String key) {
        logger.info("load(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            return file.resolveFile(key).getContent().getInputStream();
        } catch (FileSystemException exception) {
            logger.warn("load(): exception=" + exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public void save(String key, InputStream is) {
        logger.info("save(): key=" + key);
        StoreHelper.validateKey(key);
        try (OutputStream os = file.resolveFile(key).getContent().getOutputStream()) {
            long length = ByteStreams.copy(is, os);
            logger.info("save(): length=" + length);
        } catch (Exception exception) {
            logger.warn("save(): exception=" + exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public boolean delete(String key) {
        logger.info("delete(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            return file.resolveFile(key).delete();
        } catch (FileSystemException exception) {
            logger.warn("delete(): exception=" + exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public long getAvailableBytes(long usedByte) {
        logger.info("getAvailableBytes(): usedByte=" + usedByte);
        long availableBytes = totalBytes - usedByte;
        logger.info("getAvailableBytes(): availableBytes=" + availableBytes);
        return availableBytes > 0 ? availableBytes : 0;
    }
}
