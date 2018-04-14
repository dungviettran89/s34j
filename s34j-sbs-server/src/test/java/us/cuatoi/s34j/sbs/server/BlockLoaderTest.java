package us.cuatoi.s34j.sbs.server;

import com.google.common.io.ByteStreams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.operation.AvailabilityUpdater;
import us.cuatoi.s34j.sbs.core.operation.BlockLoader;
import us.cuatoi.s34j.sbs.core.operation.BlockSaver;
import us.cuatoi.s34j.sbs.core.store.model.InformationRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BlockLoaderTest {
    @Autowired
    private AvailabilityUpdater availabilityUpdater;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private BlockSaver blockSaver;
    @Autowired
    private BlockLoader blockLoader;

    @Test
    public void testSaveThenLoadOneMb() throws Exception {
        if (informationRepository.count() == 0) {
            availabilityUpdater.updateAll();
        }
        String testKey = UUID.randomUUID().toString();
        byte[] testBytes = new byte[1024 * 1024];
        long saveCount = blockSaver.save(testKey, new ByteArrayInputStream(testBytes));
        assertEquals(testBytes.length, saveCount);
        try (InputStream is = blockLoader.load(testKey)) {
            long loadCount = ByteStreams.copy(is, ByteStreams.nullOutputStream());
            assertEquals(testBytes.length, loadCount);
        }
    }
}
