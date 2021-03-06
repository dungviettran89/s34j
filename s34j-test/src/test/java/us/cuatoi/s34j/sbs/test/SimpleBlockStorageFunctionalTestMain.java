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
import us.cuatoi.s34j.spring.storage.block.BlockStorageServerImpl;
import us.cuatoi.s34j.test.TestHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
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

    private static void functionalTest(String[] args) {
        IntStream.range(0, 8).parallel().forEach((i) -> testOne());
        IntStream.range(0, 8).parallel().forEach((i) -> testImpl());
    }

    private static void testImpl() {
        try {
            BlockStorageServerImpl server = new BlockStorageServerImpl("http://localhost:19000/blocks/");
            byte[] testBytes = new byte[1024 * 1024];
            String testKey = UUID.randomUUID().toString();
            server.save(testKey, new ByteArrayInputStream(testBytes));
            long length = ByteStreams.copy(server.load(testKey), ByteStreams.nullOutputStream());
            assertEquals(testBytes.length, length);
            server.delete(testKey);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void testOne() {
        try {
            String baseUrl = "http://localhost:19000/blocks/";
            String testKey = UUID.randomUUID().toString();
            HttpURLConnection save = (HttpURLConnection) new URL(baseUrl + testKey).openConnection();
            save.setRequestMethod("PUT");
            save.setDoInput(true);
            save.setDoOutput(true);
            save.connect();
            HashFunction hashFunction = Hashing.goodFastHash(32);
            HashingOutputStream saveHash = new HashingOutputStream(hashFunction, save.getOutputStream());
            try (OutputStreamWriter writer = new OutputStreamWriter(saveHash, UTF_8)) {
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
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
