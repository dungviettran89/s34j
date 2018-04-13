package us.cuatoi.s34j.sbs.server;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.sardine.SardineConfiguration;
import us.cuatoi.s34j.sbs.core.store.sardine.SardineStoreProvider;
import us.cuatoi.s34j.sbs.core.store.vfs.VfsStoreProvider;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SardineStoreTest extends AbstractStoreTest {

    private static Store store;
    @Autowired
    private SardineStoreProvider provider;
    @Autowired
    private VfsStoreProvider vfsStoreProvider;
    @Value("${test.sardine.url:unavailable}")
    private String url;
    @Value("${test.sardine.user:unavailable}")
    private String user;
    @Value("${test.sardine.password:unavailable}")
    private String password;

    @Override
    protected Store newStore() {
        if (store == null) {
            if (equalsIgnoreCase(url, "unavailable")) {
                logger.warn("Test property not available. Testing VFS Mem instead");
                store = vfsStoreProvider.createStore("ram:///test/", null);
            } else {
                SardineConfiguration config = new SardineConfiguration();
                config.setUser(user);
                config.setPassword(password);
                store = provider.createStore(url, config);
            }
        }
        return store;
    }
}
