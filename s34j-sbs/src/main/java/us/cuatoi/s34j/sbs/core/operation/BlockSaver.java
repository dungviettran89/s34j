package us.cuatoi.s34j.sbs.core.operation;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.CountingInputStream;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
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

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class BlockSaver {
    private static final Logger logger = LoggerFactory.getLogger(BlockSaver.class);
    public static final String CANDIDATE = "candidate";
    private static final int backUpIntervalMinutes = 10;

    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private StoreCache storeCache;
    @Autowired
    private DeleteRepository deleteRepository;
    @Value("${s34j.sbs.initialCount:2}")
    private int initialCount;
    @Value("${s34j.sbs.targetCount:4}")
    private int targetCount;
    @Value("${s34j.sbs.BlockSaver.maxKeyToUpdatePerIteration:128}")
    private int maxKeyToUpdatePerIteration;

    private LoadingCache<String, List<InformationModel>> candidateCaches = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, List<InformationModel>>() {
                @Override
                public List<InformationModel> load(String key) {
                    PageRequest page = new PageRequest(0, targetCount + 1);
                    List<InformationModel> candidates = informationRepository.findByActiveOrderByAvailableBytesDesc(true, page)
                            .stream().sorted(Comparator.comparingLong(InformationModel::getLatency)).collect(Collectors.toList());
                    logger.info("getCandidateStores() candidates=" + candidates);
                    return candidates;
                }
            });

    private List<String> getSaveCandidates() {
        List<InformationModel> candidates = candidateCaches.getUnchecked("candidate");
        logger.info("getCandidateStores() candidates=" + candidates);
        List<String> shortlistedStores = candidates.stream().limit(initialCount + 1).map(InformationModel::getName)
                .collect(Collectors.toList());
        logger.info("getCandidateStores() shortlistedStores=" + shortlistedStores);
        return shortlistedStores;
    }

    public long save(String key, InputStream input) {
        logger.info("save() key=" + key);
        StoreHelper.validateKey(key);
        logger.info("save() input=" + input);
        Preconditions.checkNotNull(input);

        CountingInputStream inputCount = new CountingInputStream(input);
        Path tempFile = null;
        try {
            tempFile = saveToTemp(inputCount);
            List<String> candidateStores = getSaveCandidates();
            logger.info("save() candidateStores=" + candidateStores);
            Preconditions.checkNotNull(candidateStores);
            Preconditions.checkArgument(candidateStores.size() > 0);
            final String version = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
            Path finalTempFile = tempFile;
            long blockCount = candidateStores.parallelStream()
                    .filter((storeName) -> saveToStore(key, version, storeName, finalTempFile)).count();
            logger.info("save() blockCount=" + blockCount);
            if (blockCount == 0) {
                throw new StoreException("Can not save to any store.");
            }

            long length = inputCount.getCount();
            logger.info("save() length=" + length);

            //Delete old version if needed
            KeyModel oldVersion = keyRepository.findOne(key);
            logger.info("save() oldVersion=" + oldVersion);
            if (oldVersion != null) {
                DeleteModel deleteOldVersion = new DeleteModel();
                deleteOldVersion.setName(oldVersion.getName());
                deleteOldVersion.setVersion(oldVersion.getVersion());
                deleteOldVersion.setDeleted(System.currentTimeMillis());
                deleteRepository.save(deleteOldVersion);
                logger.info("save() deleteOldVersion=" + deleteOldVersion);
            }

            //save new version
            KeyModel newVersion = new KeyModel();
            newVersion.setName(key);
            newVersion.setSize(length);
            newVersion.setVersion(version);
            newVersion.setBlockCount(blockCount);
            keyRepository.save(newVersion);
            logger.info("save() newVersion=" + newVersion);
            return length;
        } finally {
            deleteTempFileQuietly(tempFile);
        }

    }

    private void deleteTempFileQuietly(Path tempFile) {
        logger.info("deleteTempFileQuietly() tempFile=" + tempFile);
        if (tempFile != null) {
            try {
                Files.delete(tempFile);
            } catch (IOException deleteTempFileError) {
                logger.info("deleteTempFileQuietly() deleteTempFileError=" + deleteTempFileError);
            }
        }
    }

    private boolean saveToStore(String key, String version, String storeName, Path tempFile) {
        try (InputStream is = Files.newInputStream(tempFile)) {
            logger.info("save() storeName=" + storeName);
            String internalKey = key + "-" + version;
            long size = storeCache.getStore(storeName).save(internalKey, is);
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

    @Scheduled(cron = "0 */" + backUpIntervalMinutes + " * * * *")
    @SchedulerLock(name = "BlockSaver.updateBlockCount", lockAtMostFor = (backUpIntervalMinutes + 1) * 60 * 1000)
    public void updateBlockCount() {
        long updated = keyRepository.findAllByBlockCountLessThan(targetCount, new PageRequest(0, maxKeyToUpdatePerIteration))
                .getContent().stream()
                .filter((k) -> targetCount - k.getBlockCount() > 0)
                .peek(k -> updateBlockCount(k.getName(), k.getVersion(), targetCount - k.getBlockCount()))
                .count();
        logger.info("updateBlockCount() updated=" + updated);
    }

    private void updateBlockCount(String key, String version, long more) {
        logger.info("updateBlockCount() key=" + key);
        Preconditions.checkArgument(isNotBlank(key));
        logger.info("updateBlockCount() version=" + version);
        Preconditions.checkArgument(isNotBlank(version));
        logger.info("updateBlockCount() more=" + more);
        Preconditions.checkArgument(more > 0);
        String internalKey = key + "-" + version;
        logger.info("updateBlockCount() internalKey=" + internalKey);

        List<InformationModel> saveCandidates = candidateCaches.getUnchecked(CANDIDATE);

        List<String> currentStores = blockRepository
                .findByKeyNameAndKeyVersion(key, version, new PageRequest(0, saveCandidates.size() * 2))
                .getContent().stream().map(BlockModel::getStoreName).collect(Collectors.toList());
        logger.info("updateBlockCount() currentStores=" + currentStores);
        if (currentStores.size() == 0) {
            logger.warn("updateBlockCount() cannot find any current block.");
            return;
        }

        Path tempFile = null;
        try {
            try (InputStream is = storeCache.getStore(currentStores.get(0)).load(internalKey)) {
                tempFile = saveToTemp(is);
            } catch (IOException saveToTempException) {
                logger.warn("updateBlockCount() saveToTempException=" + saveToTempException);
                return;
            }

            Path finalTempFile = tempFile;
            long saved = saveCandidates.stream().map(InformationModel::getName)
                    .filter(o -> !currentStores.contains(o))
                    .filter((storeName) -> this.saveToStore(key, version, storeName, finalTempFile))
                    .limit(more + 1).count();
            logger.info("updateBlockCount() saved=" + saved);
            if (saved == 0) return;
            KeyModel model = keyRepository.findOne(key);
            if (!equalsIgnoreCase(model.getVersion(), version)) {
                logger.info("updateBlockCount() version=" + version);
                logger.info("updateBlockCount() modelVersion=" + model.getVersion());
                return;
            }
            model.setBlockCount(model.getBlockCount() + saved);
            logger.info("updateBlockCount() model=" + model);
            keyRepository.save(model);
        } finally {
            deleteTempFileQuietly(tempFile);
        }
    }
}
