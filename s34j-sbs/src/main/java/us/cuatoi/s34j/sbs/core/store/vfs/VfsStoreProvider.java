package us.cuatoi.s34j.sbs.core.store.vfs;

import com.google.common.base.Preconditions;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            if (!folder.exists()) {
                folder.createFolder();
            }
            return new VfsStore(folder);
        } catch (FileSystemException vfsException) {
            throw new StoreException(vfsException);
        }
    }
}
