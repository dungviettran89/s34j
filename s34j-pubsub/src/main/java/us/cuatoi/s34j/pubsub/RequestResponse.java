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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RequestResponse implements RemovalListener<String, RequestResponse.CompletableFutureHolder> {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponse.class);
    @Value("${s34j.pubsub.request.response.timeoutMinutes:10}")
    private int timeout;
    @Value("${random.uuid}")
    private String instanceId;
    @Autowired
    private PubSub pubSub;
    private Set<String> consumers = new HashSet<>();
    private Cache<String, CompletableFutureHolder<?>> futures;

    @PostConstruct
    void start() {
        futures = CacheBuilder.newBuilder().expireAfterWrite(timeout, TimeUnit.MINUTES).removalListener(this).build();
    }

    public <T> CompletableFuture<T> sendRequestForResponse(Object request, Class<T> responseClass) {
        return sendRequestForResponse(request.getClass().getName(), request, responseClass.getName(), responseClass);
    }

    public <T> CompletableFuture<T> sendRequestForResponse(String requestTopic, Object request, Class<T> responseClass) {
        return sendRequestForResponse(requestTopic, request, responseClass.getName(), responseClass);
    }

    public <T> CompletableFuture<T> sendRequestForResponse(Object request, String responseTopic, Class<T> responseClass) {
        return sendRequestForResponse(request.getClass().getName(), request, responseTopic, responseClass);
    }

    public <T> CompletableFuture<T> sendRequestForResponse(String requestTopic, Object request, String responseTopic, Class<T> responseClass) {
        String uuid = UUID.randomUUID().toString();
        if (!consumers.contains(responseTopic)) {
            consumers.add(responseTopic);
            String subscriptionName = responseTopic + "_" + RequestResponse.class.getSimpleName() + "_" + instanceId;
            pubSub.register(responseTopic, subscriptionName, responseClass, (message) -> {
                this.onMessage(responseTopic, message);
            }).autoRemove();
            logger.info("Registering for new topic {}. subscriptionName={}", responseTopic, subscriptionName);
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        CompletableFutureHolder holder = new CompletableFutureHolder();
        holder.uuid = uuid;
        holder.requestTopic = requestTopic;
        holder.responseTopic = responseTopic;
        holder.future = future;
        futures.put(uuid, holder);

        pubSub.publish(requestTopic, request, ImmutableMap.of("uuid", uuid));
        return future;
    }

    @SuppressWarnings("unchecked")
    private <T> void onMessage(String responseTopic, Message<T> received) {
        String uuid = received.getHeaders().get("uuid");
        if (uuid == null) {
            logger.warn("Received unknown message from {}. payLoad={}, headers={}",
                    responseTopic, received.getPayload(), received.getHeaders());
            return;
        }

        CompletableFutureHolder holder = futures.getIfPresent(uuid);
        if (holder == null) {
            logger.warn("Received unknown {} from {}. payLoad={}, headers={}",
                    uuid, responseTopic, received.getPayload(), received.getHeaders());
            return;
        }

        holder.future.complete(received.getPayload());
        futures.invalidate(uuid);
    }


    @Override
    public void onRemoval(RemovalNotification<String, CompletableFutureHolder> notification) {
        CompletableFutureHolder holder = notification.getValue();
        if (holder.future.isDone()) return;
        String message = String.format("Timed out waiting for response for message %s. " +
                "requestTopic: %s, responseTopic: %s", holder.uuid, holder.requestTopic, holder.responseTopic);
        TimeoutException timeoutException = new TimeoutException(message);
        holder.future.completeExceptionally(timeoutException);
    }

    static class CompletableFutureHolder<T> {
        String uuid;
        String requestTopic;
        String responseTopic;
        CompletableFuture<T> future;
    }
}
