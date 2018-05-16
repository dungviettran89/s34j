package us.cuatoi.s34jserver.functional;

import com.google.common.io.ByteStreams;
import org.junit.Test;

import java.io.IOException;

public class ContentInputStreamTest {
    @Test
    public void testSkip() throws IOException {
        ByteStreams.skipFully(new ContentInputStream(3 * 1024 * 1024), 10000);
    }
}
