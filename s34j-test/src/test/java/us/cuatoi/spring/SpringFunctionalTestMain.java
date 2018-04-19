package us.cuatoi.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import us.cuatoi.s34j.test.TestHelper;
import us.cuatoi.s34jserver.functional.S34JFunctionalTest;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class SpringFunctionalTestMain {

    public static final Logger logger = LoggerFactory.getLogger(SpringFunctionalTestMain.class);

    public static void main(String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(SpringTestApplication.class, args);
        new Thread(() -> {
            await().atMost(1, TimeUnit.MINUTES)
                    .until(() -> !TestHelper.available(19000));
            logger.info("Test started");
            try {
                functionalTest(args);
            } catch (Exception testError) {
                logger.error("Test failed, error=" + testError, testError);
                TestHelper.sleep(5000);
            } finally {
                SpringApplication.exit(context);
            }
        }).start();
    }

    private static void functionalTest(String[] args) {
        S34JFunctionalTest.main(args);
        logger.info("------------------------------------------------------------------");
        logger.info("TEST COMPLETED, YES.");
        logger.info("------------------------------------------------------------------");
    }

}
