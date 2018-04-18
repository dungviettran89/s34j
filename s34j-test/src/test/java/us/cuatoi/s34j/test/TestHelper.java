package us.cuatoi.s34j.test;

import java.io.IOException;
import java.net.Socket;

public class TestHelper {
    public static final String DEFAULT_KEY = "Q3AM3UQ867SPQQA43P2F";
    public static final String DEFAULT_SECRET = "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG";

    public static boolean available(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
