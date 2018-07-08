package us.cuatoi.s34j.test.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import us.cuatoi.s34j.pubsub.*;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnablePubSub
@EnableScheduling
@PropertySource("file:local.properties")
public class PubSubTestApplication {

    public static final Logger logger = LoggerFactory.getLogger(PubSubTestApplication.class);
    @Autowired
    private PubSub pubSub;
    @Autowired
    private RequestResponse requestResponse;

    @PubSubListener
    public void onTestMessage(TestMessage message) {
        logger.info("onTestMessage message={}", message);
    }

    @PubSubListener(addUniqueSuffix = true)
    public void onTestMessage2(TestMessage message) {
        logger.info("onTestMessage2 message={}", message);
    }

    @PubSubHandler(responseTopic = "handledTestMessage")
    public String handleTestMessage(TestMessage message) {
        return "Received " + message.getMessage();
    }

    @PubSubHandler(requestTopic = "test_request", responseTopic = "test_response")
    public String requestResponseHandled(String message) {
        return "Received " + message + " at " + new Date();
    }

    @PubSubListener("handledTestMessage")
    public void onMessageReplied(String message) {
        logger.info("onMessageReplied message={}", message);
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    void sendCorrectMessage() {
        pubSub.publish(new TestMessage());
        CompletableFuture<String> response = requestResponse
                .sendRequestForResponse("test_request", "Message: " + new Date(),
                        "test_response", String.class);
        response.whenCompleteAsync((s, e) -> {
            logger.info("sendRequestForResponse s={}, ex={}", s, e);
        });
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 120 * 1000)
    void sendIncorrectMessage() {
        pubSub.publish(TestMessage.class.getName(), "String Message");
    }


}
