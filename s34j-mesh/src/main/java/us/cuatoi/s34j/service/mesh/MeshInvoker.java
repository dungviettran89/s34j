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
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import static us.cuatoi.s34j.service.mesh.MeshFilter.SM_FORWARD_INVOKE;

@Slf4j
public class MeshInvoker {
    private Cache<String, FutureHolder> futures;
    private ExecutorService executor;


    @Value("${s34j.service-mesh.invocationPoolSize:0}")
    private int poolSize;
    @Value("${s34j.service-mesh.invokeTimeoutSeconds:60}")
    private int invokeTimeoutSeconds;
    @Value("${s34j.service-mesh.forwardInvokeEnabled:false}")
    private boolean forwardInvokeEnabled;
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

    private boolean remoteInvoke(Invoke invoke) {
        String currentNodeName = nodeProvider.provide().getName();
        String service = invoke.getService();
        String to = invoke.getTo();
        log.debug("Remote invoke service {} in node {}", service, to);
        //try direct remote invoke first
        Invoke result = directInvoke(invoke);

        if (result == null) {
            //throw error if forward invoke is not enabled
            if (!forwardInvokeEnabled) {
                return returnError(invoke);
            } else {
                //Only 1 hops forwarding is supported for now, which supports a firewalled network where 1 node can acts
                //as a gateway to the internal network, also it supposed to help if network is partitioned into 2 segment
                //with 1 node can connect to both segment.
                //Support to 3 or more firewalled sub-net is possible but it would require running a full probe of
                //the network which is too expensive to maintain. This solution strikes a balance between both world
                Set<String> directNodeNames = meshManager.getMesh().getNodes().values().stream()
                        .filter((node) -> !equalsIgnoreCase(currentNodeName, node.getName()))
                        .filter((node) -> node.getServices().contains(service))
                        .filter((node) -> isBlank(to) || equalsIgnoreCase(to, node.getName()))
                        .map(Node::getName).collect(Collectors.toSet());
                List<Node> forwardingNodes = meshManager.getMesh().getNodes().values().stream()
                        .filter((node) -> !directNodeNames.contains(node.getName()))
                        .filter((node) -> !equalsIgnoreCase(node.getName(), currentNodeName))
                        .filter((node) -> {
                            Map<String, Integer> knownLatency = meshManager.getKnownLatencies().get(node.getName());
                            Set<String> adjacentNodes = knownLatency != null ? knownLatency.keySet() : new HashSet<>();
                            return Sets.intersection(adjacentNodes, directNodeNames).size() > 0;
                        })
                        .collect(Collectors.toList());
                if (forwardingNodes.size() == 0) {
                    return returnError(invoke);
                }

                Collections.shuffle(forwardingNodes);
                Node forwardingNode = forwardingNodes.get(0);
                return forwardInvoke(invoke, forwardingNode.getUrl());
            }
        }

        return returnResult(result);
    }

    private Invoke directInvoke(Invoke invoke) {
        String to = invoke.getTo();
        String service = invoke.getService();
        String currentNodeName = nodeProvider.provide().getName();
        List<Node> directNodes = meshManager.getMesh().getNodes().values().stream()
                .filter((node) -> !equalsIgnoreCase(currentNodeName, node.getName()))
                .filter((node) -> node.getServices().contains(service))
                .filter((node) -> isBlank(to) || equalsIgnoreCase(to, node.getName()))
                .collect(Collectors.toList());
        Collections.shuffle(directNodes);
        return directNodes.stream()
                .map((n) -> doDirectInvoke(n, invoke))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private boolean returnResult(Invoke result) {
        FutureHolder holder = futures.getIfPresent(result.getCorrelationId());
        if (holder != null) {
            if (isNotBlank(result.getOutputJson())) {
                Object output = meshTemplate.fromJson(result.getOutputJson(), holder.responseClass);
                holder.future.complete(output);
            } else {
                log.warn("Invoke error, exception={}, stackTrace={}", result.getException(), result.getExceptionStackTrace());
                holder.future.completeExceptionally(new RuntimeException(result.getException()));
            }
        }
        return true;
    }

    public boolean forwardInvoke(Invoke invoke, String url) {
        log.debug("Forward invoke service {} through url {}", invoke.getService(), url);
        Invoke result = meshTemplate.post(url, SM_FORWARD_INVOKE, invoke, Invoke.class);
        if (result == null) {
            return returnError(invoke);
        } else {
            return returnResult(result);
        }
    }

    private boolean returnError(Invoke invoke) {
        FutureHolder holder = futures.getIfPresent(invoke.getCorrelationId());
        if (holder != null) {
            holder.future.completeExceptionally(new RuntimeException("Can not invoke service " + invoke.getService() +
                    " due to no available node."));
        }
        return false;
    }

    private Invoke doDirectInvoke(Node node, Invoke invoke) {
        log.debug("Direct invoke service {} in node {}", invoke.getService(), node.getName());
        return meshTemplate.post(node.getUrl(), SM_DIRECT_INVOKE, invoke, Invoke.class);
    }

    public Invoke handleDirectInvoke(Invoke invoke) {

        invoke.getChain().add(nodeProvider.provide().getName());

        String service = invoke.getService();
        MeshServiceBeanPostProcessor.ServiceMethodBeanHolder holder = meshServiceBeanPostProcessor
                .getServiceMap().get(service);
        if (holder == null) {
            invoke.setException("Unknown service " + invoke.getService());
            return invoke;
        }
        Object input = meshTemplate.fromJson(invoke.getInputJson(), holder.getRequestClass());

        try {
            Object result = holder.getMethod().invoke(holder.bean, input);
            invoke.setOutputJson(meshTemplate.toJson(result));
            log.debug("Direct invoke of service {} completed successfully.", invoke.getService());
            return invoke;
        } catch (Exception ex) {
            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            invoke.setException(rootCause.getMessage());
            invoke.setExceptionStackTrace(ExceptionUtils.getStackTrace(rootCause));
            log.info("Direct invoke of service {} completed exceptionally. exception={}",
                    invoke.getService(), rootCause.getMessage(), rootCause);
            return invoke;
        }
    }

    public Invoke handleForwardInvoke(Invoke invoke) {
        invoke.getChain().add(nodeProvider.provide().getName());
        Invoke result = directInvoke(invoke);
        if (result != null) {
            log.debug("Forward invoke success service={}", invoke.getService());
            return result;
        } else {
            invoke.setException("Can not invoke service " + invoke.getService() +
                    " due to no available node.");
            log.debug("Forward invoke failed service={}", invoke.getService());
            return invoke;
        }
    }

    @Data
    static class FutureHolder {
        Class<?> responseClass;
        CompletableFuture future;
    }
}
