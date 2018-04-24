package us.cuatoi.s34j.spring.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.ObjectPartModel;
import us.cuatoi.s34j.spring.model.ObjectPartRepository;
import us.cuatoi.s34j.spring.storage.block.BlockStorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ObjectPartManager {
    private static final Logger logger = LoggerFactory.getLogger(ObjectPartManager.class);
    @Autowired
    private BlockStorage blockStorage;
    @Autowired
    private ObjectPartRepository objectPartRepository;

    public List<ObjectPartModel> saveToStore(List<InputStream> parts) {
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            return saveToStore(parts, pool);
        } catch (InterruptedException submitTaskError) {
            logger.warn("saveToStore() submitTaskError=" + submitTaskError, submitTaskError);
            throw new RuntimeException(submitTaskError);
        } finally {
            pool.shutdown();
        }
    }

    private List<ObjectPartModel> saveToStore(List<InputStream> parts, ExecutorService pool) throws InterruptedException {
        List<Callable<ObjectPartModel>> partSavers = parts.stream()
                .map((is) -> (Callable<ObjectPartModel>) () -> saveToBlock(is))
                .collect(Collectors.toList());
        boolean rollBack = false;
        List<Future<ObjectPartModel>> futures = pool.invokeAll(partSavers);
        ArrayList<ObjectPartModel> result = new ArrayList<>();
        for (Future<ObjectPartModel> future : futures) {
            try {
                result.add(future.get());
            } catch (ExecutionException saveError) {
                logger.warn("saveToStore() saveError=" + saveError, saveError);
                rollBack = true;
            }
        }
        if (rollBack) {
            for (ObjectPartModel model : result) {
                try {
                    blockStorage.delete(model.getPartName());
                } catch (IOException cleanUpError) {
                    logger.warn("saveToStore() model=" + model);
                    logger.warn("saveToStore() cleanUpError=" + cleanUpError, cleanUpError);
                    //there is nothing we can do here
                }
            }
            throw new RuntimeException("Can not save to backend.");
        }
        objectPartRepository.save(result);
        logger.info("saveToStore() result=" + result);
        return result;
    }

    private ObjectPartModel saveToBlock(InputStream is) throws IOException {
        String name = UUID.randomUUID().toString();
        try (InputStream i = is) {
            ObjectPartModel model = new ObjectPartModel();
            model.setPartName(name);
            model.setCreatedDate(System.currentTimeMillis());
            blockStorage.save(model.getPartName(), i);
            logger.info("saveToBlock() is=" + is);
            logger.info("saveToBlock() name=" + name);
            return model;
        }
    }
}
