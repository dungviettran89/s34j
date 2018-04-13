package us.cuatoi.s34j.sbs.core.operation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
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
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class StoreInformationUpdater {

    private static final Logger logger = LoggerFactory.getLogger(StoreInformationUpdater.class);
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
    @SchedulerLock(name = "StoreInformationUpdater", lockAtMostFor = (updateIntervalMinutes + 1) * 60 * 1000)
    @PostConstruct
    @VisibleForTesting
    public void perform() {
        List<String> configuredStores = new ArrayList<>();
        for (ConfigurationModel config : configurationRepository.findAll()) {
            updateInformation(config);
            configuredStores.add(config.getName());
        }
        logger.info("perform() updatedCount=" + configuredStores.size());

        List<InformationModel> unknownInformation = new ArrayList<>();
        for (InformationModel info : informationRepository.findAll()) {
            if (!configuredStores.contains(info.getName())) {
                info.setActive(false);
                unknownInformation.add(info);
                logger.info("perform() unknownInformation=" + info);
            }
        }
        informationRepository.save(unknownInformation);
        logger.info("perform() unknownCount=" + unknownInformation.size());

    }

    private void updateInformation(ConfigurationModel config) {
        logger.info("updateInformation() config=" + config);
        Preconditions.checkNotNull(config);
        if (isBlank(config.getName())) {
            logger.info("updateInformation() name=" + config.getName());
            return;
        }

        try {
            Store store = storeCache.getStore(config.getName());
            InformationModel information = new InformationModel();
            information.setName(config.getName());
            information.setAvailableBytes(store.getAvailableBytes());
            information.setTotalBytes(store.getTotalBytes());
            information.setUsedBytes(store.getUsedBytes());
            information.setActive(true);
            informationRepository.save(information);
            logger.info("updateInformation() information=" + information);
        } catch (Exception canNotLoadInformationException) {
            logger.warn("updateInformation() canNotLoadInformationException=" + canNotLoadInformationException,
                    canNotLoadInformationException);
            InformationModel information = new InformationModel();
            information.setName(config.getName());
            information.setActive(false);
            informationRepository.save(information);
            logger.info("updateInformation() information=" + information);
        }
    }
}
