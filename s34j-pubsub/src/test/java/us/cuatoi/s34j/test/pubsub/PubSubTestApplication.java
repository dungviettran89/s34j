package us.cuatoi.s34j.test.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import us.cuatoi.s34j.pubsub.EnablePubSub;
import us.cuatoi.s34j.pubsub.PubSub;

@SpringBootApplication
@EnablePubSub
@EnableScheduling
@PropertySource("file:local.properties")
public class PubSubTestApplication {

    public static final Logger logger = LoggerFactory.getLogger(PubSubTestApplication.class);
    private boolean registered = false;
    @Autowired
    private PubSub pubSub;


    @Scheduled(fixedDelay = 5000)
    void sendMessage() {
        pubSub.publish(new TestMessage());
        if (!registered) {
            registered = true;
            pubSub.register(TestMessage.class, (message) -> {
                logger.info("Received message={}", message);
            });
        }
    }
}
