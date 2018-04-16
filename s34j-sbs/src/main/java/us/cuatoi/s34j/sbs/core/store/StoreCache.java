package us.cuatoi.s34j.sbs.core.store;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationModel;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationRepository;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class StoreCache {

    private static final Logger logger = LoggerFactory.getLogger(StoreCache.class);
    private final Cache<String, Store> storeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();
    @Autowired
    private ConfigurationRepository configurationRepository;
    @Autowired
    private List<StoreProvider> providers;
    private Map<String, StoreProvider> providerMap = new ConcurrentHashMap<>();

    @PostConstruct
    void start() {
        logger.info("start() providers=" + providers);
        providers.forEach((configuredProvider) -> {
            providerMap.put(configuredProvider.getType(), configuredProvider);
            logger.info("start()  configuredType=" + configuredProvider.getType());
            logger.info("start()  configuredProvider=" + configuredProvider);
        });
    }

    public Store getStore(String name) {
        logger.info("getStore() name=" + name);
        Preconditions.checkNotNull(name);

        try {
            return storeCache.get(name, () -> {
                ConfigurationModel configuration = configurationRepository.findOne(name);
                logger.info("getStore() configuration=" + configuration);
                Preconditions.checkNotNull(configuration);

                StoreProvider provider = providerMap.get(configuration.getType());
                logger.info("getStore() provider=" + provider);
                Preconditions.checkNotNull(provider);

                Object storeConfiguration = null;
                if (isNotBlank(configuration.getJson())) {
                    storeConfiguration = new Gson().fromJson(configuration.getJson(), provider.getConfigClass());
                    logger.info("getStore() storeConfiguration=" + storeConfiguration);
                }
                return provider.createStore(configuration.getUri(), (Serializable) storeConfiguration);
            });
        } catch (ExecutionException executionException) {
            logger.info("getStore() executionException=" + executionException);
            throw new StoreException(executionException);
        }
    }


}
