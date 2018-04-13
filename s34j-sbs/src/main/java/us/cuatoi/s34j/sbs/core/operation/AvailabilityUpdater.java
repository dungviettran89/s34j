package us.cuatoi.s34j.sbs.core.operation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
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

import javax.annotation.PostConstruct;
import java.io.OutputStream;
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
     * Perform the update of the information of stores
     */
    @Scheduled(cron = "0 */" + updateIntervalMinutes + " * * * *")
    @SchedulerLock(name = "AvailabilityUpdater", lockAtMostFor = (updateIntervalMinutes + 1) * 60 * 1000)
    @PostConstruct
    @VisibleForTesting
    public void updateAll() {
        Iterable<ConfigurationModel> allStores = configurationRepository.findAll();
        Set<String> checkedStores = Lists
                .newArrayList(allStores)
                .parallelStream()
                .peek(this::updateOne)
                .map(ConfigurationModel::getName)
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

    private void updateOne(ConfigurationModel config) {
        logger.info("updateAvailability() config=" + config);
        Preconditions.checkNotNull(config);
        Preconditions.checkArgument(isNotBlank(config.getName()));

        Store store = storeCache.getStore(config.getName());
        Preconditions.checkNotNull(store);

        //check availability by test write
        //we may need to evaluate to use 2 flag instead: canRead and canWrite
        boolean active = false;
        Stopwatch watch = Stopwatch.createStarted();
        try {
            logger.info("updateAvailability() storeToTest=" + store);
            byte[] oneMB = new byte[1024 * 1024];
            String testKey = UUID.randomUUID().toString();
            logger.info("updateAvailability() testKey=" + testKey);
            try (OutputStream os = store.save(testKey)) {
                os.write(oneMB);
            }
            store.delete(testKey);
            active = true;
        } catch (Exception writeException) {
            logger.warn("updateAvailability() writeException=" + writeException, writeException);
        }
        logger.info("updateAvailability() active=" + active);
        logger.info("updateAvailability() latency=" + watch.elapsed(TimeUnit.MILLISECONDS));

        //save information
        InformationModel info = informationRepository.findOne(config.getName());
        if (info == null) {
            info = new InformationModel();
            info.setName(config.getName());
        }
        info.setActive(active);
        info.setLatency(watch.elapsed(TimeUnit.MILLISECONDS));
        informationRepository.save(info);
        logger.info("updateAvailability() info=" + info);
    }

}
