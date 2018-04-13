package us.cuatoi.s34j.sbs.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileSystemUtils;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationModel;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationRepository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
    private List<Path> testDirs = new ArrayList<>();

    @PostConstruct
    void start() throws IOException {
        logger.info("start()");
        createTestStore("spring-test-1");
        createTestStore("spring-test-2");
        createTestStore("spring-test-3");
    }


    private void createTestStore(String name) throws IOException {
        Path testDir = Files.createTempDirectory(name);
        testDirs.add(testDir);
        ConfigurationModel store = new ConfigurationModel();
        store.setName(name);
        store.setType("nio");
        store.setUri(testDir.toUri().toString());
        configurationRepository.save(store);
        logger.info("createTestStore() name=" + name);
        logger.info("createTestStore() uri=" + store.getUri());
    }

    @PreDestroy
    void stop() {
        configurationRepository.deleteAll();
        testDirs.stream()
                .map(Path::toFile)
                .forEach(deletedDir -> {
                    boolean deleteResult = FileSystemUtils.deleteRecursively(deletedDir);
                    logger.info("stop() deletedDir=" + deletedDir);
                    logger.info("stop() deleteResult=" + deleteResult);
                });
    }
}
