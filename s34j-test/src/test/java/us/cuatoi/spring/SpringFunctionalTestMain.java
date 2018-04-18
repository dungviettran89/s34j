package us.cuatoi.spring;

import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import us.cuatoi.s34j.test.TestHelper;

import java.util.UUID;
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
                simpleFunctionalTest();
            } catch (Exception testError) {
                logger.error("Test failed, error=" + testError, testError);
                TestHelper.sleep(5000);
            } finally {
                SpringApplication.exit(context);
            }
        }).start();
    }

    private static void simpleFunctionalTest() throws Exception {
        MinioClient client = new MinioClient("http://localhost:19000",
                TestHelper.DEFAULT_KEY, TestHelper.DEFAULT_SECRET);
        client.makeBucket("test" + UUID.randomUUID().toString());
    }
}
