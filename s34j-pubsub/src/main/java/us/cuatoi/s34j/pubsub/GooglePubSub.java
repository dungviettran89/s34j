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
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class GooglePubSub implements PubSub {

    public static final Logger logger = LoggerFactory.getLogger(GooglePubSub.class);
    @Autowired
    private DestinationConfigurationProvider configurationProvider;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InstantiatingExecutorProvider executorProvider;
    private LoadingCache<String, Publisher> publishers = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Publisher>() {
                @Override
                public Publisher load(String destination) {
                    return getPublisher(destination);
                }
            });

    @Override
    public <T extends Serializable> void register(String destination, String name, Class<T> tClass, Consumer<T> consumer) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public void publish(String destination, Object objectMessage) {
        try {
            String json = objectMapper.writeValueAsString(objectMessage);
            ByteString data = ByteString.copyFrom(json, StandardCharsets.UTF_8);
            PubsubMessage message = PubsubMessage.newBuilder().setData(data).build();
            publishers.getUnchecked(destination).publish(message);
            logger.info("Published message. destination={}, json={}", destination, json);
        } catch (JsonProcessingException serializationError) {
            logger.error("serializationError, destination={}, objectMessage={}",
                    destination, objectMessage, serializationError);
            throw new RuntimeException(serializationError);
        }
    }

    private Publisher getPublisher(String destination) {
        DestinationConfiguration configuration = configurationProvider.getConfiguration(destination);
        String project = configuration.getProject();
        try {
            GoogleCredentials googleCredentials = getCredentials(destination);
            TopicName topicName = TopicName.of(project, destination);
            FixedCredentialsProvider credentialsProvider = new FixedCredentialsProvider() {
                @Nullable
                @Override
                public Credentials getCredentials() {
                    return googleCredentials;
                }
            };
            Publisher publisher = Publisher.newBuilder(topicName).setCredentialsProvider(credentialsProvider)
                    .setExecutorProvider(executorProvider).build();
            logger.info("Created published. destination={} project={}", destination, project);
            return publisher;
        } catch (IOException createPublisherException) {
            logger.error("createPublisherException, destination={}, project={}", destination, project, createPublisherException);
            throw new RuntimeException(createPublisherException);
        }
    }

    private GoogleCredentials getCredentials(String destination) {
        DestinationConfiguration configuration = configurationProvider.getConfiguration(destination);
        try {
            return GoogleCredentials.fromStream(new ByteArrayInputStream(configuration.getKey()));
        } catch (IOException canNotLoadCredential) {
            logger.error("canNotLoadCredential, destination={}", destination, canNotLoadCredential);
            throw new IllegalArgumentException("Invalid credential.", canNotLoadCredential);
        }
    }
}
