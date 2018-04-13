package us.cuatoi.s34j.sbs.core.store.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreException;
import us.cuatoi.s34j.sbs.core.store.StoreProvider;

import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class NioStoreProvider implements StoreProvider<NioConfiguration> {

    public static final Logger logger = LoggerFactory.getLogger(NioStoreProvider.class);

    @Override
    public String getType() {
        return "nio";
    }

    @Override
    public Class<NioConfiguration> getConfigClass() {
        return NioConfiguration.class;
    }

    @Override
    public Store createStore(String uriString, NioConfiguration config) {
        logger.info("createStore() uriString=" + uriString);
        logger.info("createStore() config=" + config);
        Path baseDir = getBaseDir(uriString, config);
        logger.info("createStore() baseDir=" + baseDir);
        NioStore path = new NioStore(baseDir);
        logger.info("createStore() path=" + path);
        return path;
    }

    private Path getBaseDir(String uriString, NioConfiguration config) {
        URI baseUri = URI.create(uriString);
        try {
            return Paths.get(baseUri);
        } catch (FileSystemNotFoundException resolveException) {
            logger.info("getBaseDir() resolveException=" + resolveException);
            try {
                config = config == null ? new NioConfiguration() : config;
                return FileSystems.newFileSystem(baseUri, config).getPath(baseUri.getPath());
            } catch (Exception newFileStoreException) {
                logger.info("getBaseDir() newFileStoreException=" + newFileStoreException);
                throw new StoreException(newFileStoreException);
            }

        }
    }
}
