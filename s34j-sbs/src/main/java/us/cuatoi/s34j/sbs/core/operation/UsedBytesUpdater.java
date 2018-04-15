package us.cuatoi.s34j.sbs.core.operation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    @Value("${s34j.sbs.UsedBytesUpdater.minBlockSize:1048576}")
    private long minBlockSize;
    @Value("${s34j.sbs.UsedBytesUpdater.sampleSize:1024}")
    private int sampleSize;

    @Scheduled(cron = "0 */" + updateIntervalMinutes + " * * * *")
    @SchedulerLock(name = "UsedBytesUpdater", lockAtMostFor = (updateIntervalMinutes + 1) * 60 * 1000)
    @PostConstruct
    public void update() {
        ArrayList<InformationModel> allInfo = Lists.newArrayList(informationRepository.findAll());
        logger.info("update() allInfo=" + allInfo);
        allInfo.forEach(i -> updateOne(i.getName(), sampleSize));
    }

    /**
     * Estimate used bytes using the average block size
     *
     * @param name       store name
     * @param sampleSize to estimate
     */
    @VisibleForTesting
    public void updateOne(String name, int sampleSize) {
        logger.info("updateOne() name=" + name);
        Page<BlockModel> sample = blockRepository.findByStoreNameAndSizeGreaterThan(name, minBlockSize / 2,
                new PageRequest(0, sampleSize));
        logger.info("updateOne() sampleSize=" + sample.getNumberOfElements());
        long bigBlockCount = sample.getTotalElements();
        logger.info("updateOne() bigBlockCount=" + bigBlockCount);
        long averageUsedBytes = (long) sample.getContent().stream().mapToLong(BlockModel::getSize).average().orElse(0);
        logger.info("updateOne() averageUsedBytes=" + averageUsedBytes);
        averageUsedBytes = averageUsedBytes > minBlockSize ? averageUsedBytes : minBlockSize;

        long bigBlockUsedBytes = averageUsedBytes * bigBlockCount;
        logger.info("updateOne() bigBlockUsedBytes=" + bigBlockUsedBytes);
        long blockCount = blockRepository.countByStoreName(name);
        logger.info("updateOne() blockCount=" + blockCount);
        long smallBlockCount = blockCount - bigBlockCount;
        logger.info("updateOne() smallBlockCount=" + blockCount);
        long smallBlockUsedBytes = minBlockSize * smallBlockCount;
        logger.info("updateOne() smallBlockUsedBytes=" + smallBlockUsedBytes);
        long usedBytes = bigBlockUsedBytes + smallBlockUsedBytes;
        logger.info("updateOne() usedBytes=" + usedBytes);

        InformationModel info = informationRepository.findOne(name);
        info.setUsedBytes(usedBytes);
        informationRepository.save(info);
        logger.info("updateOne() info=" + info);

    }
}
