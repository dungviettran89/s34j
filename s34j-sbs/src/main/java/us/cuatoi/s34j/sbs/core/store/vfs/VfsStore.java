package us.cuatoi.s34j.sbs.core.store.vfs;

import com.google.common.base.Preconditions;
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

    public static final Logger logger = LoggerFactory.getLogger(VfsStore.class);
    public static final long oneGB = 1024 * 1024 * 1024;
    private final FileObject file;

    public VfsStore(FileObject folder) throws FileSystemException {
        logger.info("VfsStore() folder=" + folder);
        Preconditions.checkNotNull(folder);
        Preconditions.checkArgument(folder.isFolder());
        this.file = folder;
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
    public OutputStream save(String key) {
        logger.info("save(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            return file.resolveFile(key).getContent().getOutputStream();
        } catch (FileSystemException exception) {
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
    public long getTotalBytes() {
        return 2 * oneGB;
    }

    @Override
    public long getUsedBytes() {
        return oneGB;
    }

    @Override
    public long getAvailableBytes() {
        return oneGB;
    }
}
