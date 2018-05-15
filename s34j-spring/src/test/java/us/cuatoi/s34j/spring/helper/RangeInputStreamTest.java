package us.cuatoi.s34j.spring.helper;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class RangeInputStreamTest {
    @Test
    public void testRange() throws IOException {
        byte[] buffer = new byte[1024 * 1024];
        new Random().nextBytes(buffer);
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        InputStream is = new RangeInputStream(bais, buffer.length, "bytes=1-1024,2048-6321,19631-");
        for (int i = 1; i < 1024; i++) {
            assertEquals(buffer[i], (byte) is.read());
        }
        for (int i = 2048; i < 6321; i++) {
            assertEquals(buffer[i], (byte) is.read());
        }
        for (int i = 19631; i < buffer.length; i++) {
            assertEquals(buffer[i], (byte) is.read());
        }
        is.close();
    }

    @Test
    public void testSuffix() throws IOException {
        byte[] buffer = new byte[1024 * 1024];
        new Random().nextBytes(buffer);
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        InputStream is = new RangeInputStream(bais, buffer.length, "bytes=1-1024,-9632");
        for (int i = 1; i < 1024; i++) {
            assertEquals(buffer[i], (byte) is.read());
        }
        for (int i = buffer.length - 9632; i < buffer.length; i++) {
            assertEquals(buffer[i], (byte) is.read());
        }
        is.close();
    }
}
