package us.cuatoi.s34j.sbs.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.uri.UriModel;
import us.cuatoi.s34j.sbs.core.uri.UriRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UriRepositoryTest {
    @Autowired
    private UriRepository uriRepository;

    @Test
    public void testSaveLoad() {
        UriModel uri = new UriModel();
        uri.setName("test");
        uri.setType("file");
        uri.setScheme("file");
        uri.setPath("data");
        uriRepository.save(uri);

        uri.setName("test-2");
        uri.setType("webdav");
        uri.setScheme("https");
        uri.setUserInfo("test:test");
        uri.setPath("/");
        uri.setPort(443);
        uriRepository.save(uri);
        for (UriModel uriModel : uriRepository.findAll()) {
            System.out.println(uriModel);
        }
        uriRepository.delete("test");
        uriRepository.delete("test-2");
    }
}
