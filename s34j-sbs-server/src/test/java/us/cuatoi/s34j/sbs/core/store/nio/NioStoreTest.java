package us.cuatoi.s34j.sbs.core.store.nio;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import us.cuatoi.s34j.sbs.core.store.AbstractStoreTest;

import java.nio.file.Path;

public class NioStoreTest extends AbstractStoreTest {
    @Override
    protected NioStore getStore() {
        Path baseDir = Jimfs.newFileSystem(Configuration.unix()).getPath("/");
        return new NioStore(baseDir);
    }
}
