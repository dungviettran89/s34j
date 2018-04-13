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
        ConfigurationModel config = new ConfigurationModel();
        config.setName("test");
        config.setType("nio");
        config.setJson(null);
        config.setUri("jimfs://");
        configurationRepository.save(config);

        config.setName("test-2");
        config.setType("webdav");
        config.setJson("{username:'test',password:'test'}");
        config.setUri("https://dav.box.net/dav/");
        configurationRepository.save(config);
        for (ConfigurationModel configurationModel : configurationRepository.findAll()) {
            System.out.println(configurationModel);
        }
        configurationRepository.delete("test");
        configurationRepository.delete("test-2");
    }
}
