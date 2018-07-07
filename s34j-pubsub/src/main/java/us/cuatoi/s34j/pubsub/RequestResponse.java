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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RequestResponse implements RemovalListener<String, RequestResponse.CompletableFutureHolder> {
    @Value("${s34j.pubsub.request.response.timeoutMinutes:10}")
    private int timeout;
    @Autowired
    private PubSub pubSub;
    private Map<String, Consumer<?>> consumers = new ConcurrentHashMap<>();
    private Cache<String, CompletableFutureHolder> futures;

    @PostConstruct
    private void start() {
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
        if (!consumers.containsKey(responseTopic)) {
            pubSub.register(responseTopic, RequestResponse.class.getName(), responseClass, new Consumer<T>() {
                @Override
                public void accept(T t) {

                }
            });
        }
        String uuid = UUID.randomUUID().toString();
        CompletableFuture<T> future = new CompletableFuture<>();
        CompletableFutureHolder holder = new CompletableFutureHolder();
        holder.uuid = uuid;
        holder.requestTopic = requestTopic;
        holder.responseTopic = responseTopic;
        holder.future = future;
        futures.put(uuid, holder);
        return future;
    }

    @Override
    public void onRemoval(RemovalNotification<String, CompletableFutureHolder> notification) {
        CompletableFutureHolder holder = notification.getValue();
        String message = String.format("Timed out waiting for response for message %s. " +
                "requestTopic: %s, responseTopic: %s", holder.uuid, holder.requestTopic, holder.responseTopic);
        TimeoutException timeoutException = new TimeoutException(message);
        holder.future.completeExceptionally(timeoutException);
    }

    static class CompletableFutureHolder {
        String uuid;
        String requestTopic;
        String responseTopic;
        CompletableFuture<?> future;
    }
}
