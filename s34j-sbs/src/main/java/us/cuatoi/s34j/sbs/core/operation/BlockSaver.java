package us.cuatoi.s34j.sbs.core.operation;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.CountingInputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.StoreHelper;
import us.cuatoi.s34j.sbs.core.store.StoreCache;
import us.cuatoi.s34j.sbs.core.store.StoreException;
import us.cuatoi.s34j.sbs.core.store.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BlockSaver {
    private static final Logger logger = LoggerFactory.getLogger(BlockSaver.class);

    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private StoreCache storeCache;
    @Value("${s34j.sbs.initialCount:2}")
    private int initialCount;
    @Value("${s34j.sbs.targetCount:3}")
    private int targetCount;
    private LoadingCache<String, List<String>> candidateCaches = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, List<String>>() {
                @Override
                public List<String> load(String key) throws Exception {
                    return getCandidateStores();
                }
            });

    private List<String> getCandidateStores() {
        List<InformationModel> candidates = informationRepository
                .findByActiveOrderByAvailableBytesDesc(true, new PageRequest(0, targetCount + 1));
        logger.info("getCandidateStores() candidates=" + candidates);
        List<String> shortlistedStores = candidates
                .stream().sorted(Comparator.comparingLong(InformationModel::getLatency)).limit(initialCount + 1)
                .map(InformationModel::getName)
                .collect(Collectors.toList());
        logger.info("getCandidateStores() shortlistedStores=" + shortlistedStores);
        return shortlistedStores;
    }

    public void save(String key, InputStream input) {
        logger.info("save() key=" + key);
        StoreHelper.validateKey(key);
        logger.info("save() input=" + input);
        Preconditions.checkNotNull(input);

        CountingInputStream inputCount = new CountingInputStream(input);
        Path tempFile = saveToTemp(inputCount);

        List<String> candidateStores = getCandidates();
        logger.info("save() candidateStores=" + candidateStores);
        Preconditions.checkNotNull(candidateStores);
        Preconditions.checkArgument(candidateStores.size() > 0);
        final String version = System.currentTimeMillis() + UUID.randomUUID().toString();
        long blockCount = candidateStores.parallelStream()
                .filter((storeName) -> {
                    logger.info("save() storeName=" + storeName);
                    try (InputStream is = Files.newInputStream(tempFile)) {
                        long size = storeCache.getStore(storeName).save(key, is);
                        logger.info("save() size=" + size);
                        BlockModel block = new BlockModel();
                        block.setKeyName(key);
                        block.setKeyVersion(version);
                        block.setStoreName(storeName);
                        block.setSize(size);
                        blockRepository.save(block);
                        logger.info("save() block=" + block);
                        return true;
                    } catch (Exception storeSaveError) {
                        logger.info("save() storeSaveError=" + storeSaveError);
                        return false;
                    }
                }).count();
        logger.info("save() blockCount=" + blockCount);
        if (blockCount == 0) {
            throw new StoreException("Can not save to any store.");
        }

        KeyModel keyModel = new KeyModel();
        keyModel.setName(key);
        keyModel.setSize(inputCount.getCount());
        keyModel.setVersion(version);
        keyModel.setBlockCount(blockCount);
        keyRepository.save(keyModel);
        logger.info("save() keyModel=" + keyModel);

    }

    private List<String> getCandidates() {

        return candidateCaches.getUnchecked("candidate");
    }

    private Path saveToTemp(InputStream input) {
        Path tempFile;
        try {
            tempFile = Files.createTempFile("BlockSaver", ".tmp");
            logger.info("saveToTemp() tempFile=" + tempFile);
            try (OutputStream os = Files.newOutputStream(tempFile)) {
                int length = IOUtils.copy(input, os);
                logger.info("saveToTemp() length=" + length);
                return tempFile;
            }
        } catch (IOException createTempFileError) {
            throw new StoreException(createTempFileError);
        }
    }
}
