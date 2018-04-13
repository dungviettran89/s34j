package us.cuatoi.s34j.sbs.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.operation.AvailabilityUpdater;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationModel;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationRepository;
import us.cuatoi.s34j.sbs.core.store.model.InformationModel;
import us.cuatoi.s34j.sbs.core.store.model.InformationRepository;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AvailabilityUpdaterTest {
    private static final Logger logger = LoggerFactory.getLogger(AvailabilityUpdaterTest.class);
    @Autowired
    private AvailabilityUpdater availabilityUpdater;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private ConfigurationRepository configurationRepository;

    @Test
    public void testCheckAvailability() {
        InformationModel information = new InformationModel();
        information.setName("unknown-information");
        information.setActive(true);
        information.setAvailableBytes(1);
        information.setUsedBytes(1);
        informationRepository.save(information);
        availabilityUpdater.updateAll();
        for (ConfigurationModel config : configurationRepository.findAll()) {
            InformationModel info = informationRepository.findOne(config.getName());
            logger.info("testCheckAvailability() info=" + info);
            assertNotNull(info);
            assertEquals(info.getName(), config.getName());
        }

        List<InformationModel> sortedInformation = informationRepository.findByActiveOrderByAvailableBytesDesc(true, new PageRequest(0, 2));
        for (InformationModel sorted : sortedInformation) {
            logger.info("testCheckAvailability() sorted=" + sorted);
            assertNotNull(sorted);

        }
    }
}
