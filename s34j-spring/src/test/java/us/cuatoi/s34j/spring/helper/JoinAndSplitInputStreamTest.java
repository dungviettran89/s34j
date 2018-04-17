package us.cuatoi.s34j.spring.helper;

import com.google.common.io.CharStreams;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JoinAndSplitInputStreamTest {

    public static final Logger logger = LoggerFactory.getLogger(JoinAndSplitInputStreamTest.class);
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testJoinAndSplit() throws IOException {
        String testPhrase = "This is a test phrase. 123456789";
        logger.info("testJoinAndSplit() testPhrase.length=" + testPhrase.length());
        SplitOutputStream split = new SplitOutputStream(8);
        try (OutputStream os = split) {
            byte[] bytes = testPhrase.getBytes(StandardCharsets.UTF_8);
            logger.info("testJoinAndSplit() bytes.length=" + bytes.length);
            os.write(bytes);
        }
        List<InputStream> streams = split.getInputStreams();
        assertNotNull(streams);
        logger.info("testJoinAndSplit() streams.size()=" + streams.size());
        assertTrue(streams.size() > 0);
        List<Callable<InputStream>> callables = streams.stream()
                .map((is) -> (Callable<InputStream>) () -> is)
                .collect(Collectors.toList());
        try (JoinInputStream is = new JoinInputStream(callables)) {
            String readPhrase = CharStreams.toString(new InputStreamReader(is));
            assertEquals(testPhrase, readPhrase);
        }
    }

}
