package us.cuatoi.s34j.sbs.core.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.StoreHelper;
import us.cuatoi.s34j.sbs.core.store.StoreCache;
import us.cuatoi.s34j.sbs.core.store.model.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BlockLoader {
    private static final Logger logger = LoggerFactory.getLogger(BlockLoader.class);

    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private StoreCache storeCache;

    public InputStream load(String key) throws FileNotFoundException {
        logger.info("load() key=" + key);
        StoreHelper.validateKey(key);

        KeyModel keyModel = keyRepository.findOne(key);
        logger.info("load() keyModel=" + keyModel);
        if (keyModel == null) {
            throw new FileNotFoundException();
        }

        List<String> availableStores = blockRepository
                .findByKeyNameAndKeyVersion(keyModel.getName(), keyModel.getVersion(), new PageRequest(0, 32))
                .getContent().stream().map(BlockModel::getStoreName).collect(Collectors.toList());
        logger.info("load() availableStores=" + availableStores);
        if (availableStores == null || availableStores.size() == 0) {
            keyRepository.delete(keyModel);
            logger.info("load() deleted keyModel=" + keyModel);
            throw new FileNotFoundException();
        }

        String internalKey = keyModel.getName() + "-" + keyModel.getVersion();
        logger.info("load() internalKey=" + internalKey);
        InputStream inputStream = informationRepository.findByActiveOrderByAvailableBytesDesc(true).stream()
                .filter((i) -> availableStores.contains(i.getName()))
                .sorted(Comparator.comparingLong(InformationModel::getLatency))
                .map(InformationModel::getName)
                .map(storeCache::getStore)
                .map(store -> {
                    try {
                        InputStream storeInputStream = store.load(internalKey);
                        logger.info("load() store=" + store);
                        logger.info("load() storeInputStream=" + storeInputStream);
                        return storeInputStream;
                    } catch (Exception loadError) {
                        logger.warn("load() loadError=" + loadError, loadError);
                        return null;
                    }
                })
                .filter(Objects::nonNull).limit(1)
                .findFirst().orElse(null);
        logger.info("load() inputStream=" + inputStream);
        if (inputStream == null) {
            throw new FileNotFoundException();
        }
        return inputStream;
    }
}
