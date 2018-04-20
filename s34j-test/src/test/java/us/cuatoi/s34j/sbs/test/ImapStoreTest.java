package us.cuatoi.s34j.sbs.test;

import org.apache.commons.vfs2.FileSystemException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.imap.ImapConfiguration;
import us.cuatoi.s34j.sbs.core.store.imap.ImapStoreProvider;
import us.cuatoi.s34j.sbs.core.store.vfs.VfsStoreProvider;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static us.cuatoi.s34j.sbs.test.VfsTmpStoreTest.createVfsTestDir;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImapStoreTest extends AbstractStoreTest {

    private static Store store;
    @Autowired
    private ImapStoreProvider provider;
    @Autowired
    private VfsStoreProvider vfsStoreProvider;
    @Value("${test.imap.url:}")
    private String url;
    @Value("${test.imap.user:}")
    private String user;
    @Value("${test.imap.email:}")
    private String email;
    @Value("${test.imap.password:}")
    private String password;

    @Override
    protected Store newStore() throws FileSystemException {
        if (store == null) {
            if (isBlank(url)) {
                store = vfsStoreProvider.createStore(createVfsTestDir("tmp:///test/"), null);
            } else {
                ImapConfiguration config = new ImapConfiguration();
                config.setEmail(email);
                config.setUser(user);
                config.setPassword(password);
                config.setFolder("test");
                config.setTotalBytes(10L * 1024 * 1024 * 1024); //10GB
                store = provider.createStore(url, config);
            }
        }
        return store;
    }
}
