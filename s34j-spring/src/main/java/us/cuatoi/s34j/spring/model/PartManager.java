package us.cuatoi.s34j.spring.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.storage.block.BlockStorage;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static us.cuatoi.s34j.spring.VersionHelper.newVersion;

@Service
public class PartManager {
    public static final Logger logger = LoggerFactory.getLogger(PartManager.class);
    private final ExecutorService pool = Executors.newCachedThreadPool();
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private DeletedPartRepository deletedPartRepository;
    @Autowired
    private BlockStorage blockStorage;

    public void deletePart(List<PartModel> partsToDelete) {
        List<DeletedPartModel> deletedParts = partsToDelete.stream()
                .map((pm) -> {
                    DeletedPartModel deleted = new DeletedPartModel();
                    deleted.setPartName(pm.getPartName());
                    deleted.setDeletedDate(System.currentTimeMillis());
                    deleted.setDeleteId(UUID.randomUUID().toString());
                    return deleted;
                })
                .collect(Collectors.toList());
        deletedPartRepository.save(deletedParts);
        partRepository.delete(partsToDelete);
        logger.info("deletePart() partsToDelete=" + partsToDelete);
        logger.info("deletePart() deletedParts=" + deletedParts);
    }

    public List<String> savePart(List<InputStream> parts) {
        List<String> savedParts = new ArrayList<>();
        List<Callable<String>> savers = parts.stream().map((is) -> (Callable<String>) () -> {
            try (InputStream i = is) {
                String partName = newVersion();
                blockStorage.save(partName, i);
                logger.info("savePart() partName=" + partName);
                return partName;
            }
        }).collect(Collectors.toList());
        boolean rollBack = false;
        try {
            List<Future<String>> futures = pool.invokeAll(savers);
            for (Future<String> future : futures) {
                try {
                    savedParts.add(future.get());
                } catch (ExecutionException savePartError) {
                    logger.warn("savePart() savePartError=" + savePartError, savePartError);
                    rollBack = true;
                }
            }
            if (rollBack) {
                for (String savedPart : savedParts) {
                    try {
                        blockStorage.delete(savedPart);
                    } catch (IOException rollBackError) {
                        logger.warn("savePart() rollBackError=" + rollBackError, rollBackError);
                    }
                }
                throw new RuntimeException("Can not save parts.");
            }
        } catch (InterruptedException unexpectedError) {
            logger.error("savePart() unexpectedError=" + unexpectedError, unexpectedError);
            throw new RuntimeException(unexpectedError);
        }
        return savedParts;
    }

    @PreDestroy
    public void stop() {
        pool.shutdown();
    }
}
