package us.cuatoi.s34jserver;

import org.springframework.boot.SpringApplication;
import us.cuatoi.s34jserver.functional.S34JFunctionalTest;

import java.io.IOException;
import java.net.Socket;

public class S34jServerApplicationFunctionTestMain {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 30 && available(19000); i++) {
                    sleep(2000);
                }
                S34JFunctionalTest.main(args);
            } catch (Exception e) {
                sleep(5000);
                throw new RuntimeException(e);
            }
        }).start();
        SpringApplication.run(S34jServerApplication.class, args);

    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean available(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }
}
