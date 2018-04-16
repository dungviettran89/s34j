package us.cuatoi.s34j.sbs.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.operation.UsedBytesUpdater;
import us.cuatoi.s34j.sbs.core.store.model.BlockModel;
import us.cuatoi.s34j.sbs.core.store.model.BlockRepository;
import us.cuatoi.s34j.sbs.core.store.model.InformationModel;
import us.cuatoi.s34j.sbs.core.store.model.InformationRepository;

import java.util.ArrayList;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UsedBytesUpdaterTest {
    public static final int testSize = 8;
    public static final Logger logger = LoggerFactory.getLogger(UsedBytesUpdaterTest.class);
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private UsedBytesUpdater usedBytesUpdater;

    @Test
    public void testUpdateBytes() {
        InformationModel im = new InformationModel();
        im.setName(getClass().getSimpleName() + "-1");
        informationRepository.save(im);

        ArrayList<BlockModel> bms = new ArrayList<>();
        bms.add(createBlock("1", im.getName()));
        bms.add(createBlock("2", im.getName()));
        bms.add(createBlock("3", im.getName()));
        blockRepository.save(bms);

        usedBytesUpdater.update();
        im = informationRepository.findOne(im.getName());
        long usedBytes = im.getUsedBytes();
        logger.info("testUpdateBytes() usedBytes=" + usedBytes);
        Assert.assertTrue(usedBytes >= testSize * bms.size());

        blockRepository.delete(bms);

        usedBytesUpdater.update();
        im = informationRepository.findOne(im.getName());
        usedBytes = im.getUsedBytes();
        logger.info("testUpdateBytes() usedBytes=" + usedBytes);
        Assert.assertTrue(usedBytes == 0);

        usedBytesUpdater.update();


        informationRepository.delete(im);
    }

    private BlockModel createBlock(String name, String storeName) {
        BlockModel bm = new BlockModel();
        bm.setId(UUID.randomUUID().toString());
        bm.setKeyName(getClass().getSimpleName() + name);
        bm.setKeyVersion(UUID.randomUUID().toString());
        bm.setStoreName(storeName);
        bm.setSize(testSize);
        return bm;
    }
}
