package us.cuatoi.s34j.sbs.test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.vfs.VfsStoreProvider;

import java.net.URI;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VfsTmpStoreTest extends AbstractStoreTest {

    private static Store store;
    @Autowired
    private VfsStoreProvider provider;

    public static String createVfsTestDir(String uriString) throws FileSystemException {
        FileObject testDir = VFS.getManager().resolveFile(URI.create(uriString));
        if (!testDir.exists()) testDir.createFolder();
        return uriString;
    }

    @Override
    protected Store newStore() throws FileSystemException {
        if (store == null) {
            store = provider.createStore(createVfsTestDir("tmp://test/"), null);
        }
        return store;
    }
}
