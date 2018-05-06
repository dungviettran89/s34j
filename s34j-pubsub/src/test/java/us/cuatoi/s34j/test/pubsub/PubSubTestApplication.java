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

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnablePubSub
@EnableScheduling
@PropertySource("file:local.properties")
public class PubSubTestApplication {

    public static final Logger logger = LoggerFactory.getLogger(PubSubTestApplication.class);
    @Autowired
    private PubSub pubSub;

    @PostConstruct
    void start() {
        pubSub.register(TestMessage.class, (message) -> {
            logger.info("Received message={}", message);
        });
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 120 * 1000)
    void sendCorrectMessage() {
        pubSub.publish(new TestMessage());
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 120 * 1000)
    void sendIncorrectMessage() {
        pubSub.publish(TestMessage.class.getName(), "String Message");
    }

}
