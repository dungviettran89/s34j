package us.cuatoi.s34j.sbs.server;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.vfs.VfsStoreProvider;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VfsTmpStoreTest extends AbstractStoreTest {

    private static Store store;
    @Autowired
    private VfsStoreProvider provider;

    @Override
    protected Store newStore() {
        if (store == null) {
            store = provider.createStore("tmp://test/", null);
        }
        return store;
    }
}
