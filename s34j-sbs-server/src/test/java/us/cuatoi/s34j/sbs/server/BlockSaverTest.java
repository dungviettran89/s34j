package us.cuatoi.s34j.sbs.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.operation.AvailabilityUpdater;
import us.cuatoi.s34j.sbs.core.operation.BlockSaver;
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

    @Test
    public void testSaveOneMb() throws Exception {
        if (informationRepository.count() == 0) {
            availabilityUpdater.updateAll();
        }
        byte[] testBytes = new byte[1024 * 1024];
        long saveCount = blockSaver.save(UUID.randomUUID().toString(), new ByteArrayInputStream(testBytes));
        assertEquals(testBytes.length, saveCount);
        blockSaver.updateBlockCount();
        usedBytesUpdater.update();
    }
}
