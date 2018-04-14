package us.cuatoi.s34j.sbs.core.store.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreException;
import us.cuatoi.s34j.sbs.core.store.StoreProvider;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;

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
        URI baseUri = URI.create(uriString);
        Path baseDir = getBaseDir(baseUri, config);
        logger.info("createStore() baseDir=" + baseDir);
        NioStore path = new NioStore(baseDir, config);
        logger.info("createStore() path=" + path);
        return path;
    }

    private Path getBaseDir(URI baseUri, NioConfiguration config) {
        try {
            return Paths.get(baseUri);
        } catch (FileSystemNotFoundException ex) {
            try {
                FileSystem newFileSystem = FileSystems.newFileSystem(baseUri, config);
                logger.info("getBaseDir() newFileSystem=" + newFileSystem);
                return newFileSystem.getPath(baseUri.getPath());
            } catch (IOException newFileSystemException) {
                logger.error("getBaseDir() newFileSystemException=" + newFileSystemException, newFileSystemException);
                throw new StoreException(newFileSystemException);
            }
        }
    }

}
