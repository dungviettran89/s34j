package us.cuatoi.s34j.sbs.core.operation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.StoreHelper;
import us.cuatoi.s34j.sbs.core.store.StoreCache;
import us.cuatoi.s34j.sbs.core.store.StoreException;
import us.cuatoi.s34j.sbs.core.store.model.*;

import java.io.FileNotFoundException;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class BlockDeleter {
    private static final Logger logger = LoggerFactory.getLogger(BlockDeleter.class);
    private static final int cleanUpIntervalMinutes = 15;

    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private DeleteRepository deleteRepository;
    @Autowired
    private StoreCache storeCache;
    @Autowired
    private BlockRepository blockRepository;
    @Value("${s34j.sbs.BlockDeleter.deleteAfterSeconds:3600}") //default to 1 hour.
    private long deleteAfterSeconds;
    @Value("${s34j.sbs.BlockDeleter.maxKeyToDeletePerIteration:128}")
    private int maxKeyToDeletePerIteration;
    @Value("${s34j.sbs.initialBlockCount:1}")
    private int initialBlockCount;

    public void delete(String key) throws FileNotFoundException {
        logger.info("delete() key=" + key);
        StoreHelper.validateKey(key);

        KeyModel currentVersion = keyRepository.findOne(key);
        logger.info("delete() currentVersion=" + currentVersion);
        if (currentVersion == null) {
            throw new FileNotFoundException();
        }

        Page<BlockModel> blocksToDelete = blockRepository.findByKeyNameAndKeyVersion(currentVersion.getName(),
                currentVersion.getVersion(), new PageRequest(0, initialBlockCount));
        logger.info("delete() blocksToDelete=" + blocksToDelete);
        long deleted = blocksToDelete.getContent().parallelStream()
                .filter(this::deleteBlock)
                .count();
        logger.info("delete() deleted=" + deleted);

        DeleteModel versionToDelete = new DeleteModel();
        versionToDelete.setName(currentVersion.getName());
        versionToDelete.setVersion(currentVersion.getVersion());
        versionToDelete.setDeleted(System.currentTimeMillis());
        logger.info("delete() versionToDelete=" + versionToDelete);
        keyRepository.delete(currentVersion);
    }

    @Scheduled(cron = "0 */" + cleanUpIntervalMinutes + " * * * *")
    @SchedulerLock(name = "BlockDeleter.cleanUpBlocks", lockAtMostFor = (cleanUpIntervalMinutes + 1) * 60 * 1000)
    public void cleanUpBlocks() {
        cleanUpBlocks(System.currentTimeMillis() - deleteAfterSeconds * 1000);
    }

    @VisibleForTesting
    public void cleanUpBlocks(long deleteBefore) {
        logger.info("cleanUpBlocks() deleteBefore=" + deleteBefore);
        logger.info("cleanUpBlocks() deleteBefore=" + new Date(deleteBefore));
        long deleted = deleteRepository.findByDeletedLessThan(deleteBefore, new PageRequest(0, maxKeyToDeletePerIteration))
                .getContent().stream()
                .filter(this::deleteKey)
                .count();
        logger.info("cleanUpBlocks() deleted=" + deleted);
    }

    private boolean deleteKey(DeleteModel keyToDelete) {
        logger.info("cleanUpBlocks() keyToDelete=" + keyToDelete);
        String key = keyToDelete.getName();
        String version = keyToDelete.getVersion();
        Page<BlockModel> blocksToDelete = blockRepository.findByKeyNameAndKeyVersion(key,
                version, new PageRequest(0, maxKeyToDeletePerIteration));
        logger.info("cleanUpBlocks() totalBlock=" + blocksToDelete.getTotalElements());
        long blockDeletedCount = blocksToDelete.getContent().stream().filter(this::deleteBlock).count();
        logger.info("cleanUpBlocks() blockDeletedCount=" + blockDeletedCount);
        boolean fullyCleaned = blockDeletedCount == blocksToDelete.getTotalElements();
        logger.info("cleanUpBlocks() fullyCleaned=" + fullyCleaned);
        if (fullyCleaned) {
            deleteRepository.delete(keyToDelete);
            logger.info("cleanUpBlocks() Deleted keyToDelete=" + keyToDelete);
        }
        return fullyCleaned;
    }

    private boolean deleteBlock(BlockModel block) {
        try {
            logger.info("deleteBlock() block=" + block);
            Preconditions.checkNotNull(block);
            Preconditions.checkArgument(isNotBlank(block.getKeyName()));
            Preconditions.checkArgument(isNotBlank(block.getKeyVersion()));
            Preconditions.checkArgument(isNotBlank(block.getStoreName()));

            String internalKey = block.getKeyName() + "-" + block.getKeyVersion();
            boolean deleted = storeCache.getStore(block.getStoreName()).delete(internalKey);
            if (deleted) {
                blockRepository.delete(block);
                logger.info("deleteBlock() deletedBlock=" + block);

            }
            return deleted;
        } catch (StoreException deleteError) {
            logger.warn("deleteBlock() deleteError=" + deleteError, deleteError);
            return false;
        }
    }
}
