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
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({Publisher.class, GoogleCredentials.class})
public class PubSubConfig {

    public static final Logger logger = LoggerFactory.getLogger(PubSubConfig.class);
    @Value("${s34j.pubsub.project:}")
    String project;
    @Value("${s34j.pubsub.credentialBase64:}")
    String credentialBase64;

    @Bean
    DestinationConfigurationProvider destinationConfigurationProvider() {
        logger.info("project={}", project);
        byte[] key = BaseEncoding.base64().decode(credentialBase64);
        return (destination) -> new DestinationConfiguration().withProject(project).withKey(key);
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    InstantiatingExecutorProvider instantiatingExecutorProvider() {
        return InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(Runtime.getRuntime()
                .availableProcessors()).build();
    }

    @Bean
    PubSub pubSub() {
        return new GooglePubSub();
    }
}
