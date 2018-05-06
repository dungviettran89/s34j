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

package us.cuatoi.s34j.pubsub.configuration;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class DefaultDestinationConfigurationProvider implements DestinationConfigurationProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDestinationConfigurationProvider.class);
    private final Environment environment;
    private final String defaultProject;
    private final String defaultCredential;
    private final LoadingCache<String, DestinationConfiguration> configurations = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, DestinationConfiguration>() {
                @Override
                public DestinationConfiguration load(String topic) {
                    return createConfiguration(topic);
                }
            });

    public DefaultDestinationConfigurationProvider(Environment environment) {
        this.environment = environment;
        this.defaultProject = environment.getProperty("s34j.pubsub.project");
        this.defaultCredential = environment.getProperty("s34j.pubsub.credentialBase64");
        Preconditions.checkArgument(defaultProject != null && defaultProject.length() > 0,
                "Please setup default project id using s34j.pubsub.project");
        Preconditions.checkArgument(defaultCredential != null && defaultCredential.length() > 0,
                "Please setup default credential using s34j.pubsub.credentialBase64");

        logger.info("Default project {}", defaultProject);
    }

    @Override
    public DestinationConfiguration getConfiguration(String topic) {
        return configurations.getUnchecked(topic);
    }

    private DestinationConfiguration createConfiguration(String topic) {
        Preconditions.checkNotNull(topic);

        String project = environment.getProperty(topic + ".project");
        String credentialBase64 = environment.getProperty(topic + ".credentialBase64");
        boolean credentialCorrect = project != null &&
                project.length() > 0 &&
                credentialBase64 != null &&
                credentialBase64.length() > 0;
        if (credentialCorrect) {
            logger.info("Loaded project {} for topic {}", project, topic);
            return new DestinationConfiguration().withProject(project)
                    .withKey(BaseEncoding.base64().decode(credentialBase64));
        }

        return new DestinationConfiguration().withProject(defaultProject)
                .withKey(BaseEncoding.base64().decode(defaultCredential));
    }
}
