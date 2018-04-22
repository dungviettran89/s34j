package us.cuatoi.s34j.sbs.test;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.hash.HashingOutputStream;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import us.cuatoi.s34j.test.TestHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

public class SimpleBlockStorageFunctionalTestMain {
    public static final Logger logger = LoggerFactory.getLogger(SimpleBlockStorageFunctionalTestMain.class);

    public static void main(String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(SimpleBlockStorageTestApplication.class, args);
        new Thread(() -> {
            await().atMost(1, TimeUnit.MINUTES)
                    .until(() -> !TestHelper.available(19000));
            logger.info("Test started");
            try {
                functionalTest(args);
            } catch (Exception testError) {
                logger.error("Test failed, error=" + testError, testError);
                TestHelper.sleep(10000);
            } finally {
                SpringApplication.exit(context);
            }
        }).start();
    }

    private static void functionalTest(String[] args) throws IOException {
        testOne();
        IntStream.range(0, 5).parallel().forEach((i) -> {
            try {
                testOne();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void testOne() throws IOException {
        String baseUrl = "http://localhost:19000/blocks/";
        String testKey = UUID.randomUUID().toString();
        HttpURLConnection save = (HttpURLConnection) new URL(baseUrl + testKey).openConnection();
        save.setRequestMethod("PUT");
        save.setDoInput(true);
        save.setDoOutput(true);
        save.connect();
        HashFunction hashFunction = Hashing.goodFastHash(32);
        HashingOutputStream saveHash = new HashingOutputStream(hashFunction, save.getOutputStream());
        try (OutputStreamWriter writer = new OutputStreamWriter(saveHash)) {
            for (int i = 0; i < 1000; i++) {
                writer.write(System.currentTimeMillis() + "-" + UUID.randomUUID().toString() + "\n");
            }
        }
        try (InputStream is = save.getInputStream()) {
            ByteStreams.copy(is, ByteStreams.nullOutputStream());
        }
        save.disconnect();

        HttpURLConnection load = (HttpURLConnection) new URL(baseUrl + testKey).openConnection();
        load.setRequestMethod("GET");
        load.setDoInput(true);
        load.setDoOutput(true);
        load.connect();
        try (HashingInputStream loadHash = new HashingInputStream(hashFunction, load.getInputStream())) {
            ByteStreams.copy(loadHash, ByteStreams.nullOutputStream());
            assertEquals(loadHash.hash().toString(), saveHash.hash().toString());
        }

        execute("DELETE", baseUrl + testKey);
        execute("GET", baseUrl + "_status");
    }

    private static void execute(String method, String url) throws IOException {
        HttpURLConnection delete = (HttpURLConnection) new URL(url).openConnection();
        delete.setRequestMethod(method);
        delete.setDoInput(true);
        delete.setDoOutput(true);
        delete.connect();
        try (InputStream is = delete.getInputStream()) {
            ByteStreams.copy(is, System.out);
        }
        delete.disconnect();
    }
}
