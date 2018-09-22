/*
 * Copyright (C) 2018 dungviettran89@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

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

import static us.cuatoi.s34j.spring.helper.StorageHelper.newVersion;

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

    public List<PartModel> savePart(List<InputStream> parts) {
        List<PartModel> savedParts = new ArrayList<>();
        List<Callable<PartModel>> savers = parts.stream().map((is) -> (Callable<PartModel>) () -> {
            try (InputStream i = is) {
                String partName = newVersion();
                long length = blockStorage.save(partName, i);
                logger.info("savePart() partName=" + partName);
                logger.info("savePart() length=" + length);
                PartModel model = new PartModel();
                model.setPartName(partName);
                model.setLength(length);
                return model;
            }
        }).collect(Collectors.toList());
        boolean rollBack = false;
        try {
            List<Future<PartModel>> futures = pool.invokeAll(savers);
            for (Future<PartModel> future : futures) {
                try {
                    savedParts.add(future.get());
                } catch (ExecutionException savePartError) {
                    logger.warn("savePart() savePartError=" + savePartError, savePartError);
                    rollBack = true;
                }
            }
            if (rollBack) {
                for (PartModel savedPart : savedParts) {
                    try {
                        blockStorage.delete(savedPart.getPartName());
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
    void stop() {
        pool.shutdown();
    }
}
