package us.cuatoi.s34j.sbs.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.store.StoreModel;
import us.cuatoi.s34j.sbs.core.store.StoreRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StoreRepositoryTest {
    @Autowired
    private StoreRepository storeRepository;

    @Test
    public void testSaveLoad() {
        StoreModel uri = new StoreModel();
        uri.setName("test");
        uri.setType("file");
        uri.setScheme("file");
        uri.setPath("data");
        storeRepository.save(uri);

        uri.setName("test-2");
        uri.setType("webdav");
        uri.setScheme("https");
        uri.setUserInfo("test:test");
        uri.setPath("/");
        uri.setPort(443);
        storeRepository.save(uri);
        for (StoreModel storeModel : storeRepository.findAll()) {
            System.out.println(storeModel);
        }
        storeRepository.delete("test");
        storeRepository.delete("test-2");
    }
}
