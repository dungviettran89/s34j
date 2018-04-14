package us.cuatoi.s34j.sbs.core.operation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.store.model.BlockModel;
import us.cuatoi.s34j.sbs.core.store.model.BlockRepository;
import us.cuatoi.s34j.sbs.core.store.model.InformationModel;
import us.cuatoi.s34j.sbs.core.store.model.InformationRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Service
public class UsedBytesUpdater {
    private static final Logger logger = LoggerFactory.getLogger(UsedBytesUpdater.class);
    private static final int updateIntervalMinutes = 30;

    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private InformationRepository informationRepository;

    @Scheduled(cron = "0 */" + updateIntervalMinutes + " * * * *")
    @SchedulerLock(name = "UsedBytesUpdater", lockAtMostFor = (updateIntervalMinutes + 1) * 60 * 1000)
    @PostConstruct
    @VisibleForTesting
    public void update() {
        ArrayList<InformationModel> allInfo = Lists.newArrayList(informationRepository.findAll());
        logger.info("update() allInfo=" + allInfo);
        allInfo.forEach(i -> updateOne(i.getName()));
    }

    private void updateOne(String name) {
        long usedBytes = blockRepository.findByStoreName(name).stream()
                .mapToLong(BlockModel::getSize)
                .sum();
        InformationModel info = informationRepository.findOne(name);
        info.setUsedBytes(usedBytes);
        informationRepository.save(info);
        logger.info("updateOne() info=" + info);

    }
}
