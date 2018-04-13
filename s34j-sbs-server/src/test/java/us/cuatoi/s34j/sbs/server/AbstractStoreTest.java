package us.cuatoi.s34j.sbs.server;

import com.google.common.io.CharStreams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.sbs.core.store.Store;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public abstract class AbstractStoreTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private Store store;

    @Before
    public void setUp() throws IOException {
        if (store == null) {
            store = newStore();
        }
    }

    @Test
    public void testKeyWithNotSupportedChars() {
        exception.expect(IllegalArgumentException.class);
        store.has("key with not supported chars");
    }

    @Test
    public void testKeyWithSlash() {
        exception.expect(IllegalArgumentException.class);
        store.has("key/with/slash");
    }

    @Test
    public void testValidKey() {
        store.has("valid.key.with.dot");
    }

    @Test
    public void testSimpleOperation() throws IOException {
        logger.info("testSimpleOperation() store.total=" + store.getTotalBytes());
        assertTrue(store.getTotalBytes() >= 0);
        logger.info("testSimpleOperation() store.available=" + store.getAvailableBytes());
        assertTrue(store.getAvailableBytes() >= 0);
        logger.info("testSimpleOperation() store.used=" + store.getUsedBytes());
        assertTrue(store.getUsedBytes() >= 0);

        String testKey = "test.txt";
        byte[] testBytes = testKey.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = store.save(testKey)) {
            os.write(testBytes);
        }
        logger.info("testSimpleOperation() store.has=" + store.has(testKey));
        assertTrue(store.has(testKey));
        logger.info("testSimpleOperation() store.size=" + store.size(testKey));
        assertTrue(store.size(testKey) > 0);

        try (InputStream is = store.load(testKey)) {
            String read = CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8));
            assertEquals(testKey, read);
        }
        assertTrue(store.delete(testKey));
        assertFalse(store.has(testKey));
    }

    protected abstract Store newStore() throws IOException;
}
