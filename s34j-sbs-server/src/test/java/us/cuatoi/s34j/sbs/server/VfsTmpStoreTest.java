package us.cuatoi.s34j.sbs.server;

import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.vfs.VfsStoreProvider;

public class VfsTmpStoreTest extends AbstractStoreTest {

    private static Store store;

    @Override
    protected Store newStore() {
        if (store == null) {
            store = new VfsStoreProvider().createStore("tmp://test/", null);
        }
        return store;
    }
}
