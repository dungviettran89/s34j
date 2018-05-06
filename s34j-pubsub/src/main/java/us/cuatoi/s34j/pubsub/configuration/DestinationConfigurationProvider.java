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

/**
 * Provide configuration for a topic
 */
public interface DestinationConfigurationProvider {
    /**
     * Provide configuration for a topic. This method can be used to provide different configuration for different topic
     * thus enable connecting to multiple google project.
     * Default implementation of this interfaces loads configuration from Spring configuration. It can also be extended
     * to load configuration from a database.
     *
     * @param topic name
     * @return configuration used for this object.
     */
    DestinationConfiguration getConfiguration(String topic);
}
