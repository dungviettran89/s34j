package us.cuatoi.s34j.sbs.core.store.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreProvider;

import java.net.URI;
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
    public Store createStore(String uri, NioConfiguration config) {
        logger.info("createStore() uri=" + uri);
        logger.info("createStore() config=" + config);
        NioStore path = new NioStore(Paths.get(URI.create(uri)));
        logger.info("createStore() path=" + path);
        return path;
    }
}
