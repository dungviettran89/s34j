# S34J Pub Sub
A google pub sub library to be used in Spring
## Installation
Installation using gradle
```groovy
compile 'us.cuatoi.s34j:s34j-pubsub:0.7.3'
```
Installation using maven
```xml
<dependency>
    <groupId>us.cuatoi.s34j</groupId>
    <artifactId>s34j-pubsub</artifactId>
    <version>0.7.3</version>
</dependency>
```
## Quick Start
Provide projectId and base64 credential using properties
```properties
s34j.pubsub.project=pubsub-demo
s34j.pubsub.credentialBase64=ew0KICAidHlwZS...
```
Annotate your application with EnablePubSub annotation
```java
@SpringBootApplication
@EnablePubSub
public class PubSubTestApplication {
    
}
``` 
Annotate method with PubSubListener to receive message
```java
public class PubSubTestApplication {
    @PubSubListener
    public void onTestMessage(TestMessage message) { 
        logger.info("onTestMessage message={}", message);
    }
}
```
Publish a message using PubSub
```java
public class PubSubTestApplication {
    @Autowired
    private PubSub pubSub;
    
    public void sendSomeMessage() { 
        pubSub.publish(new TestMessage());
    }
}
```
## How to obtain credential to use
Copy project from dashboard page
![Copy project from dashboard page](s34j-pubsub-projectId.PNG)

Go to IAM & admin > Service Accounts
![Go to IAM & admin > Service Accounts](s34j-pubsub-createKey.PNG)

Create a new key with json format
![Create a new key with json format](s34j-pubsub-create-key-json.PNG)

Download and format key to base64
![Download and format key to base64](s34j-pubsub-base64.PNG)

Use the key in property file
```properties
s34j.pubsub.credentialBase64=ew0KICAidHlwZS...
```
## Advanced use cases
Provide projectId and base64 credential for each topic by specifying **topic_name**.project and
 **topic_name**.credentialBase64. S34J Pub Sub will load and use credential for that topic
 
```properties
sample_topic.project=pubsub-demo
sample_topic.credentialBase64=ew0KICAidHlwZS...
```

Use addUniqueSuffix to add an unique suffix to subscription name. This is used when you want to broadcast a message
to all server.

```java
public class PubSubTestApplication {
    @PubSubListener(addUniqueSuffix = true)
    public void onTestMessage(TestMessage message) { 
    }
}
```

Use PubSubHandler to quickly send response to a channel
```java
@PubSubHandler(requestTopic = "test_request", responseTopic = "test_response")
public String requestResponseHandled(String message) {
    return "Received " + message + " at " + new Date();
}
```
PubSubHandler can be used in junction with RequestResponse to quickly route message using Google Pub Sub
```java
@Autowired
private RequestResponse requestResponse;

CompletableFuture<String> response = requestResponse
    .sendRequestForResponse("test_request", "Message: " + new Date(), "test_response", String.class);
response.whenCompleteAsync((s, e) -> {
    logger.info("sendRequestForResponse s={}, ex={}", s, e);
});
```