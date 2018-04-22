package us.cuatoi.s34j.sbs.core.operation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreCache;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationModel;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationRepository;
import us.cuatoi.s34j.sbs.core.store.model.InformationModel;
import us.cuatoi.s34j.sbs.core.store.model.InformationRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class AvailabilityUpdater {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityUpdater.class);
    private static final int updateIntervalMinutes = 5;
    @Autowired
    private ConfigurationRepository configurationRepository;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private StoreCache storeCache;

    /**
     * Update store availability
     */
    @Scheduled(cron = "0 */" + updateIntervalMinutes + " * * * *")
    @SchedulerLock(name = "AvailabilityUpdater", lockAtMostFor = (updateIntervalMinutes + 1) * 60 * 1000)
    @VisibleForTesting
    public void updateAll() {
        Iterable<ConfigurationModel> allStores = configurationRepository.findAll();
        List<InformationModel> updatedStores = Lists.newArrayList(allStores).parallelStream()
                .map(this::updateOne).collect(Collectors.toList());
        informationRepository.save(updatedStores);
        Set<String> checkedStores = updatedStores.stream()
                .map(InformationModel::getName)
                .collect(Collectors.toSet());
        logger.info("updateAvailability() updatedCount=" + checkedStores.size());

        Iterable<InformationModel> allInfos = informationRepository.findAll();
        long unknownCount = Lists.newArrayList(allInfos).stream()
                .filter((info) -> !checkedStores.contains(info.getName()))
                .peek((unknownInformation) -> {
                    logger.info("updateAvailability() unknownInformation=" + unknownInformation);
                    unknownInformation.setActive(false);
                    informationRepository.save(unknownInformation);
                })
                .count();
        logger.info("updateAvailability() unknownCount=" + unknownCount);

    }

    private InformationModel updateOne(ConfigurationModel config) {
        logger.info("updateAvailability() config=" + config);
        Preconditions.checkNotNull(config);
        String name = config.getName();
        Preconditions.checkArgument(isNotBlank(name));

        long usedBytes = getUsedBytes(name);
        logger.info("updateAvailability(" + name + ") usedBytes=" + usedBytes);
        //check availability by test write
        //we may need to evaluate to use 2 flag instead: canRead and canWrite
        boolean active = false;
        long availableBytes = 0;
        Stopwatch watch = Stopwatch.createStarted();
        try {
            Store store = storeCache.getStore(name);
            Preconditions.checkNotNull(store);
            logger.info("updateAvailability(" + name + ") storeToTest=" + store);
            byte[] testBytes = new byte[1024 * 1024];
            String testKey = UUID.randomUUID().toString();
            logger.info("updateAvailability() testKey=" + testKey);
            long count = store.save(testKey, new ByteArrayInputStream(testBytes));
            active = count == testBytes.length;
            availableBytes = store.getAvailableBytes(usedBytes);
            try (InputStream is = store.load(testKey)) {
                long length = ByteStreams.copy(is, ByteStreams.nullOutputStream());
                active = active && length == testBytes.length;
            }
            active = active && store.delete(testKey);
        } catch (Exception writeException) {
            logger.warn("updateAvailability() writeException=" + writeException, writeException);
        }
        logger.info("updateAvailability(" + name + ") active=" + active);
        logger.info("updateAvailability(" + name + ") latency=" + watch.elapsed(TimeUnit.MILLISECONDS));

        //save information
        InformationModel info = getInformation(name);
        info.setAvailableBytes(availableBytes);
        info.setActive(active);
        info.setLatency(watch.elapsed(TimeUnit.MILLISECONDS));
        logger.info("updateAvailability(" + name + ") info=" + info);
        return info;
    }

    private long getUsedBytes(String name) {
        InformationModel info = getInformation(name);
        return info.getUsedBytes();
    }

    private InformationModel getInformation(String name) {
        InformationModel info = informationRepository.findOne(name);
        if (info == null) {
            info = new InformationModel();
            info.setName(name);
        }
        return info;
    }

}
