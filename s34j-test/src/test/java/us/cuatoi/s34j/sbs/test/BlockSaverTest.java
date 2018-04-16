package us.cuatoi.s34j.sbs.test;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.operation.AvailabilityUpdater;
import us.cuatoi.s34j.sbs.core.operation.BlockSaver;
import us.cuatoi.s34j.sbs.core.operation.StoreStatusProvider;
import us.cuatoi.s34j.sbs.core.operation.UsedBytesUpdater;
import us.cuatoi.s34j.sbs.core.store.model.InformationRepository;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BlockSaverTest {
    @Autowired
    private AvailabilityUpdater availabilityUpdater;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private BlockSaver blockSaver;
    @Autowired
    private UsedBytesUpdater usedBytesUpdater;
    @Autowired
    private StoreStatusProvider storeStatusProvider;
    @Test
    public void testSaveOneMb() {
        if (informationRepository.count() == 0) {
            availabilityUpdater.updateAll();
        }
        byte[] testBytes = new byte[1024 * 1024];
        String testKey = UUID.randomUUID().toString();
        long saveCount = blockSaver.save(testKey, new ByteArrayInputStream(testBytes));
        assertEquals(testBytes.length, saveCount);
        blockSaver.updateBlockCount();
        //Try to overwrites
        testBytes = new byte[2 * 1024 * 1024];
        saveCount = blockSaver.save(testKey, new ByteArrayInputStream(testBytes));
        assertEquals(testBytes.length, saveCount);

        newBlock(RandomUtils.nextInt(1, 4) * 1024 * 1024);
        newBlock(RandomUtils.nextInt(1, 4) * 1024 * 1024);
        newBlock(RandomUtils.nextInt(1, 4) * 1024);
        newBlock(RandomUtils.nextInt(1, 4) * 1024);
        newBlock(RandomUtils.nextInt(1, 4) * 1024);
        blockSaver.updateBlockCount();

        informationRepository.findAll().forEach((i) -> {
            usedBytesUpdater.updateOne(i.getName(), 3);
        });

        storeStatusProvider.loadStatus();
    }

    private void newBlock(int size) {
        byte[] testBytes;
        long saveCount;
        testBytes = new byte[size];
        saveCount = blockSaver.save(UUID.randomUUID().toString(), new ByteArrayInputStream(testBytes));
        assertEquals(testBytes.length, saveCount);
    }
}
