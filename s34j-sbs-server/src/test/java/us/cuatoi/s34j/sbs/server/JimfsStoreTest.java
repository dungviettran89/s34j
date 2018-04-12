package us.cuatoi.s34j.sbs.server;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.nio.NioConfiguration;
import us.cuatoi.s34j.sbs.core.store.nio.NioStoreProvider;

public class JimfsStoreTest extends AbstractStoreTest {

    private static Store store;

    @Override
    protected Store getStore() {
        if (store == null) {
            Jimfs.newFileSystem("test", Configuration.unix());
            store = new NioStoreProvider().createStore("jimfs://test/", new NioConfiguration());
        }
        return store;
    }
}
