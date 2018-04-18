package us.cuatoi.s34jserver;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import us.cuatoi.s34j.test.TestHelper;
import us.cuatoi.s34jserver.functional.S34JFunctionalTest;

public class S34jServerApplicationFunctionTestMain {
    public static void main(String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(S34jServerApplication.class, args);

        new Thread(() -> {
            try {
                for (int i = 0; i < 30 && TestHelper.available(19000); i++) {
                    TestHelper.sleep(2000);
                }
                S34JFunctionalTest.main(args);
                System.out.println("------------------------------------------------------------------");
                System.out.println("TEST COMPLETED, YES.");
                System.out.println("------------------------------------------------------------------");
                TestHelper.sleep(5000);
            } catch (Exception e) {
                TestHelper.sleep(5000);
                e.printStackTrace();
            } finally {
                SpringApplication.exit(context);
            }
        }).start();
    }

}
