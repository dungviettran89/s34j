package us.cuatoi.s34j.test.pubsub;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Date;

public class PubSubFunctionalTestMain {
    public static void main(String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(PubSubTestApplication.class, args);
        System.out.println(new Date(context.getStartupDate()));
    }
}
