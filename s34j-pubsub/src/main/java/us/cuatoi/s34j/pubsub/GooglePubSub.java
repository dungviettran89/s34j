/*
 * Copyright (C) 2018 dungviettran89@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package us.cuatoi.s34j.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.*;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import us.cuatoi.s34j.pubsub.configuration.DestinationConfiguration;
import us.cuatoi.s34j.pubsub.configuration.DestinationConfigurationProvider;
import us.cuatoi.s34j.pubsub.log.PubSubLogger;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Google Pub Sub implementation to handle all pub/sub integration.
 */
public class GooglePubSub extends PubSub {

    private static final Logger logger = LoggerFactory.getLogger(GooglePubSub.class);
    private final Set<SubscriptionInformation> subscribers = new HashSet<>();
    private final LoadingCache<String, CredentialsProvider> credentialsProviders = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, CredentialsProvider>() {
                @Override
                public CredentialsProvider load(String topic) {
                    return createCredentialsProvider(topic);
                }
            });
    private final LoadingCache<String, ProjectTopicName> topics = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, ProjectTopicName>() {
                @Override
                public ProjectTopicName load(String topic) {
                    return createTopicName(topic);
                }
            });
    private final LoadingCache<String, Publisher> publishers = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Publisher>() {
                @Override
                public Publisher load(String topic) {
                    return createPublisher(topic);
                }
            });
    private final Environment environment;
    private final DestinationConfigurationProvider configurationProvider;
    private final ObjectMapper objectMapper;
    private final ExecutorProvider executorProvider;
    private final PubSubLogger pubSubLogger;

    public GooglePubSub(Environment environment, DestinationConfigurationProvider configurationProvider, ObjectMapper objectMapper, ExecutorProvider executorProvider, PubSubLogger pubSubLogger) {
        this.environment = environment;
        this.configurationProvider = configurationProvider;
        this.objectMapper = objectMapper;
        this.executorProvider = executorProvider;
        this.pubSubLogger = pubSubLogger;
    }

    @Override
    public <T> SubscriptionInformation register(String topic, String subscription, Class<T> messageClass, Consumer<Message<T>> consumer) {
        Preconditions.checkNotNull(topic);
        Preconditions.checkNotNull(subscription);
        Preconditions.checkNotNull(messageClass);
        Preconditions.checkNotNull(consumer);
        if (!isEmpty(getTopicPrefix())) {
            topic = getTopicPrefix() + "." + topic;
            subscription = getTopicPrefix() + "." + subscription;
        }
        String finalTopic = topic;
        String finalSubscription = subscription;
        ProjectSubscriptionName subscriptionName = getSubscriptionName(finalTopic, finalSubscription);
        MessageReceiver receiver = (message, response) -> {
            String json = message.getData().toString(UTF_8);
            pubSubLogger.logIncoming(finalTopic, finalSubscription, messageClass, json);

            String receivedClass = message.getAttributesOrDefault("class", null);
            if (!messageClass.getName().equalsIgnoreCase(receivedClass)) {
                logger.info("Ignored message due to invalid class. topic={} subscription={} messageClass={} receivedClass={} json={}",
                        finalTopic, finalSubscription, messageClass, receivedClass, json);
                return;
            }

            try {
                T t = objectMapper.readValue(json, messageClass);
                Message<T> received = new Message<>();
                received.setHeaders(message.getAttributesMap());
                received.setPayload(t);
                received.setRawPayload(message.getData());
                consumer.accept(received);
                response.ack();
                logger.debug("Acknowledged message. topic={} subscription={} messageClass={} json={}",
                        finalTopic, finalSubscription, messageClass, json);
            } catch (Exception consumeException) {
                logger.error("Can not handle message. topic={} subscription={} messageClass={} json={}",
                        finalTopic, finalSubscription, messageClass, json, consumeException);
                response.nack();
                throw new RuntimeException(consumeException);
            }
        };
        CredentialsProvider credentialsProvider = credentialsProviders.getUnchecked(topic);
        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver)
                .setCredentialsProvider(credentialsProvider).setExecutorProvider(executorProvider).build();
        subscriber.startAsync();
        SubscriptionInformation subscriptionInformation = new SubscriptionInformation(subscriber, topic, subscriptionName);
        subscribers.add(subscriptionInformation);
        return subscriptionInformation;
    }

    private String getTopicPrefix() {
        return environment.getProperty("s34j.pubsub.topicPrefix", "");
    }

    /**
     * Get a subscription name, also perform check if a subscription is available. Create a new subscription if none are
     * available
     *
     * @param topic        the subscription should listen to
     * @param subscription the name of the subscription
     * @return subscription name to be used in Google Pub Sub client
     */
    private ProjectSubscriptionName getSubscriptionName(String topic, String subscription) {
        DestinationConfiguration configuration = configurationProvider.getConfiguration(topic);
        String project = configuration.getProject();
        CredentialsProvider credentialsProvider = credentialsProviders.getUnchecked(topic);
        ProjectTopicName topicName = topics.getUnchecked(topic);
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(project, subscription);
        try {
            SubscriptionAdminSettings settings = SubscriptionAdminSettings.newBuilder()
                    .setCredentialsProvider(credentialsProvider)
                    .setExecutorProvider(executorProvider).build();
            SubscriptionAdminClient client = SubscriptionAdminClient.create(settings);
            try {
                Subscription existingSubscription = client.getSubscription(subscriptionName);
                if (topicName.toString().equalsIgnoreCase(existingSubscription.getTopic())) {
                    logger.info("Found existing subscription. subscriptionName={}", subscriptionName);
                    return subscriptionName;
                } else {
                    logger.warn("Found existing subscription but it is not bound to correct topic." +
                            " subscriptionName={} topicName={}", subscriptionName, existingSubscription.getTopic());
                    client.deleteSubscription(subscriptionName);
                    client.createSubscription(subscriptionName,
                            topicName, PushConfig.getDefaultInstance(), 600);
                    logger.info("Re-created new subscription. subscriptionName={}", subscriptionName);
                    return subscriptionName;
                }
            } catch (Exception checkException) {
                if (checkException.getMessage().contains("NOT_FOUND")) {
                    logger.info("Subscription not found, creating. subscriptionName={}", subscriptionName);
                    client.createSubscription(subscriptionName,
                            topicName, PushConfig.getDefaultInstance(), 600);
                    return subscriptionName;
                } else {
                    throw checkException;
                }
            } finally {
                client.close();
            }
        } catch (Exception createClientException) {
            logger.error("Can not create subscription admin client.subscriptionName={}",
                    subscriptionName, createClientException);
            throw new RuntimeException(createClientException);
        }
    }

    @Override
    public void publish(String topic, Object objectMessage, Map<String, String> headers) {
        Preconditions.checkNotNull(topic);
        Preconditions.checkNotNull(objectMessage);
        if (!isEmpty(getTopicPrefix())) {
            topic = getTopicPrefix() + "." + topic;
        }
        try {
            String json = objectMapper.writeValueAsString(objectMessage);
            pubSubLogger.logOutgoing(topic, objectMessage.getClass(), json);

            ByteString data = ByteString.copyFrom(json, UTF_8);
            PubsubMessage message = PubsubMessage.newBuilder().setData(data)
                    .putAllAttributes(headers)
                    .putAttributes("class", objectMessage.getClass().getName())
                    .build();
            publishers.getUnchecked(topic).publish(message);
            logger.debug("Published message. topic={}, json={}", topic, json);
        } catch (JsonProcessingException serializationError) {
            logger.error("serializationError, topic={}, objectMessage={}",
                    topic, objectMessage, serializationError);
            throw new RuntimeException(serializationError);
        }
    }

    /**
     * Create a new publisher to publish message to Google Pub/Sub
     *
     * @param topic to create publisher
     * @return a new Publisher
     */
    private Publisher createPublisher(String topic) {
        try {
            TopicName topicName = topics.getUnchecked(topic);
            CredentialsProvider credentialsProvider = credentialsProviders.getUnchecked(topic);
            Publisher publisher = Publisher.newBuilder(topicName).setCredentialsProvider(credentialsProvider)
                    .setExecutorProvider(executorProvider).build();
            logger.info("Created publisher. topic={}", topic);
            return publisher;
        } catch (IOException createPublisherException) {
            logger.error("createPublisherException, topic={}", topic, createPublisherException);
            throw new RuntimeException(createPublisherException);
        }
    }

    /**
     * Create a new Topic Name to be used by Pub Sub client. This method also check then create a new topic
     * if none is available.
     *
     * @param topic name to create
     * @return Topic Name to be used by pubsub.
     */
    private ProjectTopicName createTopicName(String topic) {
        DestinationConfiguration configuration = configurationProvider.getConfiguration(topic);
        ProjectTopicName topicName = ProjectTopicName.of(configuration.getProject(), topic);
        try {
            TopicAdminSettings settings = TopicAdminSettings.newBuilder()
                    .setCredentialsProvider(credentialsProviders.getUnchecked(topic))
                    .setExecutorProvider(executorProvider).build();
            TopicAdminClient adminClient = TopicAdminClient.create(settings);
            try {
                adminClient.getTopic(topicName);
                logger.info("Found existing topic. topicName={}", topicName);
                return topicName;
            } catch (Exception checkTopicException) {
                logger.info("Topic not found. topicName={} checkTopicException={}",
                        topicName, checkTopicException);
                if (checkTopicException.getMessage().contains("NOT_FOUND")) {
                    adminClient.createTopic(topicName);
                    return topicName;
                } else {
                    throw new RuntimeException(checkTopicException);
                }
            } finally {
                adminClient.close();
            }
        } catch (Exception createTopicException) {
            logger.error("Can not create topic. topicName={}", topicName, createTopicException);
            throw new RuntimeException(createTopicException);
        }
    }

    /**
     * Load configuration from provider then prepare a new FixedCredentialProvider to be used to authenticate
     * with google pub sub
     *
     * @param topic to be created
     * @return CredentialsProvider for this topic.
     */
    private CredentialsProvider createCredentialsProvider(String topic) {
        DestinationConfiguration configuration = configurationProvider.getConfiguration(topic);
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(configuration.getKey()));
            return new FixedCredentialsProvider() {
                @Nullable
                @Override
                public Credentials getCredentials() {
                    return credentials;
                }
            };
        } catch (IOException canNotLoadCredential) {
            logger.error("canNotLoadCredential, topic={}", topic, canNotLoadCredential);
            throw new IllegalArgumentException("Invalid credential.", canNotLoadCredential);
        }
    }

    /**
     * Clean up and stop all subscribers and publishers
     */
    @PreDestroy
    void stop() {
        subscribers.parallelStream().forEach((this::stop));
        publishers.asMap().values().parallelStream().forEach(this::stop);
    }

    private void stop(Publisher publisher) {
        try {
            publisher.shutdown();
        } catch (Exception shutdownError) {
            logger.error("Can not shutdown publisher. topic={}", publisher.getTopicName(), shutdownError);
        }
    }

    private void stop(SubscriptionInformation information) {
        try {
            information.subscriber.stopAsync();
            if (information.isAutoRemove()) {
                CredentialsProvider credentials = credentialsProviders.getUnchecked(information.topic);
                SubscriptionAdminSettings settings = SubscriptionAdminSettings.newBuilder()
                        .setCredentialsProvider(credentials)
                        .setExecutorProvider(executorProvider).build();
                SubscriptionAdminClient client = SubscriptionAdminClient.create(settings);
                client.deleteSubscription(information.subscriptionName);
                logger.info("Deleted subscription {}", information.subscriptionName);
            }
        } catch (Exception ex) {
            logger.warn("Can not clean up subscription {}", information.subscriptionName, ex);
        }
    }
}
