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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import us.cuatoi.s34j.pubsub.configuration.DefaultDestinationConfigurationProvider;
import us.cuatoi.s34j.pubsub.configuration.DestinationConfigurationProvider;
import us.cuatoi.s34j.pubsub.log.PubSubLogger;
import us.cuatoi.s34j.pubsub.log.SimplePubSubLogger;

@Configuration
@ConditionalOnClass({Publisher.class, GoogleCredentials.class})
public class PubSubConfig {
    public static final Logger logger = LoggerFactory.getLogger(GooglePubSub.class);

    @Bean
    public PubSubListenerBeanPostProcessor pubSubBeanPostProcessor(PubSub pubSub) {
        return new PubSubListenerBeanPostProcessor(pubSub);
    }

    @Bean
    public PubSubHandlerBeanPostProcessor pubSubHandlerBeanPostProcessor(PubSub pubSub) {
        return new PubSubHandlerBeanPostProcessor(pubSub);
    }

    @Bean
    @ConditionalOnMissingBean(PubSub.class)
    public PubSub pubSub(
            Environment environment,
            DestinationConfigurationProvider configurationProvider,
            PubSubLogger pubSubLogger,
            ExecutorProvider executorProvider,
            ObjectMapper objectMapper
    ) {
        return new GooglePubSub(environment, configurationProvider, objectMapper, executorProvider, pubSubLogger);
    }

    @Bean
    @ConditionalOnMissingBean(RequestResponse.class)
    public RequestResponse requestResponse(PubSub pubSub, Environment environment) {
        return new RequestResponse(pubSub, environment);
    }

    @Bean
    @ConditionalOnMissingBean(DestinationConfigurationProvider.class)
    public DestinationConfigurationProvider destinationConfigurationProvider(Environment environment) {
        logger.info("Configuration Provider not found, setting up default instance.");
        return new DefaultDestinationConfigurationProvider(environment);
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        logger.info("ObjectMapper not found, setting up default instance.");
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean(PubSubLogger.class)
    public PubSubLogger pubSubLogger() {
        logger.info("Logger not found, setting up a simple logger.");
        return new SimplePubSubLogger();
    }

    @Bean
    @ConditionalOnMissingBean(ExecutorProvider.class)
    public ExecutorProvider executorProvider() {
        logger.info("Executor Provider not found, setting up default instance.");
        return InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(Runtime.getRuntime()
                .availableProcessors()).build();
    }
}
