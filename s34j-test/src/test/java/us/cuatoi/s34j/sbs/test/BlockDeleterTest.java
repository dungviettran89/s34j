package us.cuatoi.s34j.sbs.test;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.operation.AvailabilityUpdater;
import us.cuatoi.s34j.sbs.core.operation.BlockDeleter;
import us.cuatoi.s34j.sbs.core.operation.BlockSaver;
import us.cuatoi.s34j.sbs.core.operation.UsedBytesUpdater;
import us.cuatoi.s34j.sbs.core.store.model.InformationRepository;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BlockDeleterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Autowired
    private AvailabilityUpdater availabilityUpdater;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private BlockSaver blockSaver;
    @Autowired
    private UsedBytesUpdater usedBytesUpdater;
    @Autowired
    private BlockDeleter blockDeleter;

    @Test
    public void testOperations() throws FileNotFoundException, InterruptedException {
        if (informationRepository.count() == 0) {
            availabilityUpdater.updateAll();
        }
        String testKey = UUID.randomUUID().toString();
        newBlock(testKey, RandomUtils.nextInt(1, 4) * 1024 * 1024);
        newBlock(testKey, RandomUtils.nextInt(1, 4) * 1024 * 1024);
        newBlock(testKey, RandomUtils.nextInt(1, 4) * 1024);
        newBlock(testKey, RandomUtils.nextInt(1, 4) * 1024);
        newBlock(testKey, RandomUtils.nextInt(1, 4) * 1024);
        Thread.sleep(100);
        blockDeleter.cleanUpBlocks(System.currentTimeMillis());
        blockSaver.updateBlockCount();
        updateInformation();
        blockDeleter.delete(testKey);
        Thread.sleep(100);
        blockDeleter.cleanUpBlocks(System.currentTimeMillis());
        updateInformation();
        thrown.expect(FileNotFoundException.class);
        blockDeleter.delete(testKey);
    }

    private void updateInformation() {
        informationRepository.findAll().forEach((i) -> {
            usedBytesUpdater.updateOne(i.getName(), 3);
        });
    }

    private void newBlock(String name, int size) {
        byte[] testBytes;
        long saveCount;
        testBytes = new byte[size];
        saveCount = blockSaver.save(name, new ByteArrayInputStream(testBytes));
        assertEquals(testBytes.length, saveCount);
    }
}
