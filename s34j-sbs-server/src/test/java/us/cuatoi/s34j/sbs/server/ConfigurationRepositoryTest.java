package us.cuatoi.s34j.sbs.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationModel;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConfigurationRepositoryTest {
    @Autowired
    private ConfigurationRepository configurationRepository;

    @Test
    public void testSaveLoad() {
        ConfigurationModel uri = new ConfigurationModel();
        uri.setName("test");
        uri.setType("nio");
        uri.setJson(null);
        uri.setUri("jimfs://");
        configurationRepository.save(uri);

        uri.setName("test-2");
        uri.setType("webdav");
        uri.setJson("{username:'test',password:'test'}");
        uri.setUri("https://dav.box.net/dav/");
        configurationRepository.save(uri);
        for (ConfigurationModel configurationModel : configurationRepository.findAll()) {
            System.out.println(configurationModel);
        }
        configurationRepository.delete("test");
        configurationRepository.delete("test-2");
    }
}
