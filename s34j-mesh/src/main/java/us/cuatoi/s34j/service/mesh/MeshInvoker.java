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

package us.cuatoi.s34j.service.mesh;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import us.cuatoi.s34j.service.mesh.bo.Invoke;
import us.cuatoi.s34j.service.mesh.bo.Node;
import us.cuatoi.s34j.service.mesh.providers.NodeProvider;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34j.service.mesh.MeshFilter.SM_DIRECT_INVOKE;

@Slf4j
public class MeshInvoker {
    private Cache<String, FutureHolder> futures;
    private ExecutorService executor;


    @Value("${s34j.service-mesh.invocationPoolSize:0}")
    private int poolSize;
    @Value("${s34j.service-mesh.invokeTimeoutSeconds:60}")
    private int invokeTimeoutSeconds;
    @Autowired
    private MeshManager meshManager;
    @Autowired
    private MeshServiceBeanPostProcessor meshServiceBeanPostProcessor;
    @Autowired
    private NodeProvider nodeProvider;
    @Autowired
    private MeshTemplate meshTemplate;

    @PostConstruct
    public void start() {
        poolSize = poolSize > 0 ? poolSize : Runtime.getRuntime().availableProcessors() - 1;
        poolSize = poolSize > 0 ? poolSize : 1;
        executor = Executors.newFixedThreadPool(poolSize);

        futures = CacheBuilder.newBuilder()
                .expireAfterWrite(invokeTimeoutSeconds, TimeUnit.SECONDS)
                .removalListener(this::invokeTimedOut)
                .build();
    }

    private void invokeTimedOut(RemovalNotification<String, MeshInvoker.FutureHolder> notification) {
        notification.getValue().future.completeExceptionally(new TimeoutException("Invocation timed out."));
    }


    public <T> Future<T> invoke(String service, Object input, Class<T> responseClass) {
        return invoke(service, input, responseClass, null);
    }

    private <T> Future<T> invoke(String service, Object input, Class<T> responseClass, String target) {
        //invoke locally if applicable
        String currentName = nodeProvider.provide().getName();
        boolean canInvokeLocally = target == null || equalsIgnoreCase(currentName, target);
        boolean serviceAvailableLocally = meshServiceBeanPostProcessor.getServices().contains(service);
        canInvokeLocally = canInvokeLocally && serviceAvailableLocally;
        if (canInvokeLocally) {
            log.debug("Invoke locally for service {}, node={}", service, target);
            return executor.submit(() -> {
                MeshServiceBeanPostProcessor.ServiceMethodBeanHolder holder = meshServiceBeanPostProcessor
                        .getServiceMap().get(service);
                Object result = holder.getMethod().invoke(holder.bean, input);
                return responseClass.cast(result);
            });
        }

        //Check if service available on target node
        Map<String, Node> nodes = meshManager.getMesh().getNodes();
        boolean serviceNotAvailable = equalsIgnoreCase(currentName, target) && !serviceAvailableLocally;
        serviceNotAvailable = serviceNotAvailable ||
                (isNotBlank(target) && nodes.get(target) != null && !nodes.get(target).getServices().contains(service));
        if (serviceNotAvailable) {
            throw new IllegalArgumentException("This service is not available on this node.");
        }

        String correlationId = UUID.randomUUID().toString();

        //forward invocation
        String inputJson = meshTemplate.toJson(input);
        Invoke invoke = new Invoke();
        invoke.setService(service);
        invoke.setInputJson(inputJson);
        invoke.setCorrelationId(correlationId);

        invoke.setFrom(currentName);
        invoke.setTo(target);
        invoke.setHeaders(new HashMap<>());
        invoke.setChain(Lists.newArrayList(currentName));

        FutureHolder holder = new FutureHolder();
        holder.setFuture(new CompletableFuture<>());
        holder.setResponseClass(responseClass);
        futures.put(correlationId, holder);
        executor.submit(() -> remoteInvoke(invoke));
        return holder.future;
    }

    private void remoteInvoke(Invoke invoke) {
        String service = invoke.getService();
        String to = invoke.getTo();
        log.debug("Remote invoke service {} in node {}", service, to);
        //try direct remote invoke first
        List<Node> directNodes = meshManager.getMesh().getNodes().values().stream()
                .filter((node) -> node.getServices().contains(service))
                .filter((node) -> isBlank(to) || equalsIgnoreCase(to, node.getName()))
                .collect(Collectors.toList());
        Collections.shuffle(directNodes);
        Invoke result = directNodes.stream()
                .map((n) -> directInvoke(n, invoke))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (result == null) {
            FutureHolder holder = futures.getIfPresent(invoke.getCorrelationId());
            if (holder != null)
                holder.future.completeExceptionally(new RuntimeException("Can not invoke service " + service));
            return;
        }

        FutureHolder holder = futures.getIfPresent(result.getCorrelationId());
        if (holder != null) {
            Object output = meshTemplate.fromJson(result.getOutputJson(), holder.responseClass);
            holder.future.complete(output);
        }
    }

    private Invoke directInvoke(Node node, Invoke invoke) {
        log.debug("Direct invoke service {} in node {}", invoke.getService(), node.getName());
        return meshTemplate.post(node.getUrl(), SM_DIRECT_INVOKE, invoke, Invoke.class);
    }

    public Invoke handleDirectInvoke(Invoke invoke) {
        return null;
    }

    @Data
    static class FutureHolder {
        Class<?> responseClass;
        CompletableFuture future;
    }
}
