package us.cuatoi.s34j.sbs.core.store;

import com.google.common.io.CharStreams;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractStoreTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testSimpleOperation() throws Exception {
        Store store = getStore();
        logger.info("testSimpleOperation() store.total=" + store.getTotal());
        assertTrue(store.getTotal() >= 0);
        logger.info("testSimpleOperation() store.available=" + store.getAvailable());
        assertTrue(store.getAvailable() >= 0);
        logger.info("testSimpleOperation() store.used=" + store.getUsed());
        assertTrue(store.getUsed() >= 0);

        String testKey = "/some/Very/long/key/Test=*&^!&*^!*^@.txt";
        byte[] testBytes = testKey.getBytes(StandardCharsets.UTF_8);
        store.save(testKey, new ByteArrayInputStream(testBytes));
        logger.info("testSimpleOperation() store.has=" + store.has(testKey));
        assertTrue(store.has(testKey));
        logger.info("testSimpleOperation() store.size=" + store.size(testKey));
        assertTrue(store.size(testKey) > 0);

        try (InputStream is = store.load(testKey)) {
            String read = CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8));
            logger.info("testSimpleOperation() read=" + read);
            assertEquals(testKey, read);
        }

        assertTrue(store.delete(testKey));
        assertFalse(store.has(testKey));
    }

    protected abstract Store getStore();
}
