package us.cuatoi.s34j.sbs.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.server.uri.UriModel;
import us.cuatoi.s34j.sbs.server.uri.UriRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UriRepositoryTest {
    @Autowired
    private UriRepository uriRepository;

    @Test
    public void testSaveLoad() {
        for (UriModel uriModel : uriRepository.findAll()) {
            System.out.println(uriModel);
        }
    }
}
