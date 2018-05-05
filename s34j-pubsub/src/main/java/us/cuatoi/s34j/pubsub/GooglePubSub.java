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
import com.google.api.core.AbstractApiService;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GooglePubSub extends PubSub {

    public static final Logger logger = LoggerFactory.getLogger(GooglePubSub.class);
    @Autowired
    private DestinationConfigurationProvider configurationProvider;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ExecutorProvider executorProvider;
    private Set<Subscriber> subscribers = new HashSet<>();
    private LoadingCache<String, CredentialsProvider> credentialsProviders = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, CredentialsProvider>() {
                @Override
                public CredentialsProvider load(String topic) {
                    return getCredentialsProvider(topic);
                }
            });
    private LoadingCache<String, Publisher> publishers = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Publisher>() {
                @Override
                public Publisher load(String topic) {
                    return getPublisher(topic);
                }
            });

    @Override
    public <T> void register(String topic, String subscription, Class<T> tClass, Consumer<T> consumer) {
        DestinationConfiguration configuration = configurationProvider.getConfiguration(topic);
        String project = configuration.getProject();
        SubscriptionName subscriptionName = SubscriptionName.of(project, subscription);
        MessageReceiver receiver = (message, response) -> {
            String json = message.getData().toString(UTF_8);
            try {
                T t = objectMapper.readValue(json, tClass);
                consumer.accept(t);
                response.ack();
                logger.info("Acknowledged message. topic={} subscription={} tClass={} json={}",
                        topic, subscription, tClass, json);
            } catch (IOException parseException) {
                logger.error("Can no read message. topic={} subscription={} tClass={} json={}",
                        topic, subscription, tClass, json, parseException);
            }
        };
        CredentialsProvider credentialsProvider = credentialsProviders.getUnchecked(topic);
        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).setCredentialsProvider(credentialsProvider).setExecutorProvider(executorProvider).build();
        subscriber.startAsync();
        subscribers.add(subscriber);
    }

    @Override
    public void publish(String topic, Object objectMessage) {
        try {
            String json = objectMapper.writeValueAsString(objectMessage);
            ByteString data = ByteString.copyFrom(json, UTF_8);
            PubsubMessage message = PubsubMessage.newBuilder().setData(data).build();
            publishers.getUnchecked(topic).publish(message);
            logger.info("Published message. topic={}, json={}", topic, json);
        } catch (JsonProcessingException serializationError) {
            logger.error("serializationError, topic={}, objectMessage={}",
                    topic, objectMessage, serializationError);
            throw new RuntimeException(serializationError);
        }
    }

    private Publisher getPublisher(String topic) {
        DestinationConfiguration configuration = configurationProvider.getConfiguration(topic);
        String project = configuration.getProject();
        try {
            CredentialsProvider credentialsProvider = credentialsProviders.getUnchecked(topic);
            TopicName topicName = TopicName.of(project, topic);
            Publisher publisher = Publisher.newBuilder(topicName).setCredentialsProvider(credentialsProvider)
                    .setExecutorProvider(executorProvider).build();
            logger.info("Created publisher. topic={} project={}", topic, project);
            return publisher;
        } catch (IOException createPublisherException) {
            logger.error("createPublisherException, topic={}, project={}", topic, project, createPublisherException);
            throw new RuntimeException(createPublisherException);
        }
    }

    private CredentialsProvider getCredentialsProvider(String topic) {
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

    @PreDestroy
    void stop() {
        subscribers.forEach(AbstractApiService::stopAsync);
        publishers.asMap().forEach((topic, publisher) -> {
            try {
                publisher.shutdown();
            } catch (Exception shutdownError) {
                logger.error("Can not shutdown publisher. topic={}", topic, shutdownError);
            }
        });
    }
}
