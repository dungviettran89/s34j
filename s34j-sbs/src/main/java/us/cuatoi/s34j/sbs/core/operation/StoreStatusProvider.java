package us.cuatoi.s34j.sbs.core.operation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.StoreStatus;
import us.cuatoi.s34j.sbs.core.store.model.*;

import java.util.concurrent.TimeUnit;

@Service
public class StoreStatusProvider {
    private static final Logger logger = LoggerFactory.getLogger(StoreStatusProvider.class);
    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private ConfigurationRepository configurationRepository;
    private LoadingCache<String, StoreStatus> statusCache = CacheBuilder.newBuilder()
            .expireAfterWrite(8, TimeUnit.MINUTES).build(new CacheLoader<String, StoreStatus>() {
                @Override
                public StoreStatus load(String key) {
                    return loadStatus();
                }
            });


    public StoreStatus loadStatusCached(boolean refresh) {
        logger.info("loadStatusCached() refresh=" + refresh);
        if (refresh) statusCache.invalidateAll();
        return statusCache.getUnchecked("StoreStatus");
    }

    @VisibleForTesting
    public StoreStatus loadStatus() {
        long usedBytes = 0;
        long availableBytes = 0;
        for (InformationModel info : informationRepository.findAll()) {
            usedBytes += info.getUsedBytes();
            availableBytes += info.getAvailableBytes();
        }

        StoreStatus storeStatus = new StoreStatus();
        storeStatus.setKeyCount(keyRepository.count());
        storeStatus.setBlockCount(blockRepository.count());
        storeStatus.setConfiguredStoreCount(configurationRepository.count());
        storeStatus.setActiveStoreCount(informationRepository
                .findByActiveOrderByAvailableBytesDesc(true).size());
        storeStatus.setUsedBytes(usedBytes);
        storeStatus.setAvailableBytes(availableBytes);
        logger.info("loadStatus() storeStatus=" + storeStatus);
        return storeStatus;
    }
}
