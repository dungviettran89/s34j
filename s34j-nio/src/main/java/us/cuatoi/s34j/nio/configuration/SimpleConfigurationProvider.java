package us.cuatoi.s34j.nio.configuration;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleConfigurationProvider implements ConfigurationProvider {

    private static final Logger logger = LoggerFactory.getLogger(SimpleConfigurationProvider.class);
    private Map<String, Path> stores = new HashMap<>();

    @Override
    public Storage getStorage(String name) {
        logger.info(".getStorage(): name=" + name);
        Preconditions.checkNotNull(name, "Name is null");
        Path path = stores.get(name);
        logger.info(".getStorage(): path=" + path);
        if (path == null) return null;
        return new Storage().setName(name).setPath(path);
    }

    @Override
    public Storage getNextStorage(String file) {
        logger.info(".getNextStorage(): file=" + file);
        Preconditions.checkNotNull(file, "File is null");
        logger.info(".getNextStorage(): stores.size=" + stores.size());
        if (stores.size() == 0) return null;
        int skip = ThreadLocalRandom.current().nextInt(stores.size() - 1);
        String name = stores.keySet().stream().skip(skip).findAny().orElse(null);
        logger.info(".getNextStorage(): name=" + name);
        Path path = stores.get(name);
        logger.info(".getNextStorage(): path=" + path);
        return new Storage().setName(name).setPath(path);

    }
}
