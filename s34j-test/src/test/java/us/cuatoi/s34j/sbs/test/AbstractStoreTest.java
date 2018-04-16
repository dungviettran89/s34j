package us.cuatoi.s34j.sbs.test;

import com.google.common.io.CharStreams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.sbs.core.store.Store;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        store.save("key with not supported chars", new ByteArrayInputStream(new byte[1024]));
    }

    @Test
    public void testKeyWithSlash() {
        exception.expect(IllegalArgumentException.class);
        store.save("key/with/slash", new ByteArrayInputStream(new byte[1024]));
    }

    @Test
    public void testSimpleOperation() throws IOException {
        String testKey = "test.txt";
        byte[] testBytes = testKey.getBytes(StandardCharsets.UTF_8);
        store.save(testKey, new ByteArrayInputStream(testBytes));
        try (InputStream is = store.load(testKey)) {
            String read = CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8));
            assertEquals(testKey, read);
        }
        logger.info("testSimpleOperation() store.availableBytes=" + store.getAvailableBytes(0));
        assertTrue(store.delete(testKey));
    }

    protected abstract Store newStore() throws IOException;
}
