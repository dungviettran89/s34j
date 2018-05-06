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

import com.google.common.base.Preconditions;

import java.util.function.Consumer;

/**
 * Provide main function for Google Pub/Sub to be used in spring. Application can publish a message
 * using publish method or can register for a particular topic using register method.
 */
public abstract class PubSub {
    /**
     * Register a consumer to a particular topic
     *
     * @param topic        to be listened to
     * @param subscription name of this consumer
     * @param messageClass the message class
     * @param consumer     to consume the message
     * @param <T>          message
     */
    public abstract <T> void register(String topic, String subscription, Class<T> messageClass, Consumer<T> consumer);

    /**
     * Publish a message to a topic
     *
     * @param topic   to publish to
     * @param message to publish in json format
     */
    public abstract void publish(String topic, Object message);

    /**
     * Overriding method, allows sending message to the topic equal to class name
     * @param message to publish
     */
    public void publish(Object message) {
        Preconditions.checkNotNull(message);
        publish(message.getClass().getName(), message);
    }

    /**
     * Overriding method, default register the consumer to a topic named by message class and consumer default named to
     * topic + .consumer
     *
     * @param messageClass the message class
     * @param consumer     to consume the message
     * @param <T>          message
     */
    public <T> void register(Class<T> messageClass, Consumer<T> consumer) {
        Preconditions.checkNotNull(messageClass);
        Preconditions.checkNotNull(consumer);
        register(messageClass.getName(), messageClass.getName() + ".consumer", messageClass, consumer);
    }
}
