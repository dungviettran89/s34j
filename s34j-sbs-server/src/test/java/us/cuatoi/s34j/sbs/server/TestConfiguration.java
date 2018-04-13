package us.cuatoi.s34j.sbs.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationModel;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationRepository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
class TestConfiguration {

    @Bean
    public TestConfigurator testConfigurator() {
        return new TestConfigurator();
    }
}

class TestConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(TestConfigurator.class);

    @Autowired
    private ConfigurationRepository configurationRepository;

    @PostConstruct
    void start() throws IOException {
        logger.info("start()");
        createTestStore("spring-test-1");
        createTestStore("spring-test-2");
        createTestStore("spring-test-3");
    }


    private void createTestStore(String name) {
        ConfigurationModel store = new ConfigurationModel();
        store.setName(name);
        store.setType("vfs");
        store.setUri("tmp://" + name + "/");
        configurationRepository.save(store);
        logger.info("createTestStore() name=" + name);
        logger.info("createTestStore() uri=" + store.getUri());
    }

    @PreDestroy
    void stop() {
        configurationRepository.deleteAll();

    }
}
