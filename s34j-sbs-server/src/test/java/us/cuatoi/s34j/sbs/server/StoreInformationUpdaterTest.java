package us.cuatoi.s34j.sbs.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.operation.StoreInformationUpdater;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationModel;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationRepository;
import us.cuatoi.s34j.sbs.core.store.model.InformationModel;
import us.cuatoi.s34j.sbs.core.store.model.InformationRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StoreInformationUpdaterTest {
    public static final Logger logger = LoggerFactory.getLogger(StoreInformationUpdaterTest.class);
    @Autowired
    private StoreInformationUpdater storeInformationUpdater;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private ConfigurationRepository configurationRepository;

    @Test
    public void testUpdateStorageInformation() {
        storeInformationUpdater.perform();
        for (ConfigurationModel config : configurationRepository.findAll()) {
            InformationModel info = informationRepository.findOne(config.getName());
            logger.info("testUpdateStorageInformation() info=" + info);
            assertNotNull(info);
            assertEquals(info.getName(), config.getName());
        }
    }
}
