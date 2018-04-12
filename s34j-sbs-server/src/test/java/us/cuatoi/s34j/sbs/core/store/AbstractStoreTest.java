package us.cuatoi.s34j.sbs.core.store;

import com.google.common.io.CharStreams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
            store = getStore();
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
    public void testSimpleOperation() {
        logger.info("testSimpleOperation() store.total=" + store.getTotal());
        assertTrue(store.getTotal() >= 0);
        logger.info("testSimpleOperation() store.available=" + store.getAvailable());
        assertTrue(store.getAvailable() >= 0);
        logger.info("testSimpleOperation() store.used=" + store.getUsed());
        assertTrue(store.getUsed() >= 0);

        String testKey = "test.txt";
        byte[] testBytes = testKey.getBytes(StandardCharsets.UTF_8);
        store.save(testKey, new ByteArrayInputStream(testBytes));
        logger.info("testSimpleOperation() store.has=" + store.has(testKey));
        assertTrue(store.has(testKey));
        logger.info("testSimpleOperation() store.size=" + store.size(testKey));
        assertTrue(store.size(testKey) > 0);

        store.load(testKey, (is) -> {
            String read = CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8));
            logger.info("testSimpleOperation() read=" + read);
            assertEquals(testKey, read);
        });

        assertTrue(store.delete(testKey));
        assertFalse(store.has(testKey));
    }

    protected abstract Store getStore() throws IOException;
}
