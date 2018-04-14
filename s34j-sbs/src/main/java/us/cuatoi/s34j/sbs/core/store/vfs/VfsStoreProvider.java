package us.cuatoi.s34j.sbs.core.store.vfs;

import com.google.common.base.Preconditions;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreException;
import us.cuatoi.s34j.sbs.core.store.StoreProvider;

import java.net.URI;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Apache VFS is also supported since it is more mature than nio
 */
@Service
@ConditionalOnClass(VFS.class)
public class VfsStoreProvider implements StoreProvider<VfsConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(VfsStoreProvider.class);

    @Value("${s34j.sbs.vfs.defaultTotalBytes:104857600}")
    private long defaultTotalBytes;

    @Override
    public String getType() {
        return "vfs";
    }

    @Override
    public Class<? extends VfsConfiguration> getConfigClass() {
        return VfsConfiguration.class;
    }

    @Override
    public Store createStore(String uri, VfsConfiguration config) {
        logger.info("createStore() uri=" + uri);
        Preconditions.checkNotNull(uri);
        Preconditions.checkArgument(isNotEmpty(uri));
        logger.info("createStore() config=" + config);
        try {
            FileObject folder = VFS.getManager().resolveFile(URI.create(uri));
            logger.info("createStore() folder=" + folder);
            logger.info("createStore() folder.exists=" + folder.exists());
            long totalBytes = config != null ? config.getTotalBytes() : defaultTotalBytes;
            logger.info("createStore() totalBytes=" + totalBytes);
            return new VfsStore(folder, totalBytes);
        } catch (FileSystemException vfsException) {
            throw new StoreException(vfsException);
        }
    }
}
