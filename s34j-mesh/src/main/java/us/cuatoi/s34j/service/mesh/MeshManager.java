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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import us.cuatoi.s34j.service.mesh.bo.Exchange;
import us.cuatoi.s34j.service.mesh.bo.Mesh;
import us.cuatoi.s34j.service.mesh.bo.Node;
import us.cuatoi.s34j.service.mesh.providers.ActiveProvider;
import us.cuatoi.s34j.service.mesh.providers.HostsProvider;
import us.cuatoi.s34j.service.mesh.providers.NodeProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Slf4j
public class MeshManager {

    /**
     * Store the last known exchange ot the node
     */
    private final Cache<String, Long> lastExchanges = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES).build();
    /**
     * Store the exchange latencies from this node
     */
    private final Cache<String, Integer> exchangeLatencies = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();
    /**
     * Store the exchanged latencies, this give a rough idea of how nearby node is connected to the rest of cluster
     * which allows probing of network.
     */
    private final Cache<String, Map<String, Integer>> knownLatencies = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();
    @Autowired
    private ActiveProvider activeProvider;
    @Autowired
    private HostsProvider hostsProvider;
    @Autowired
    private NodeProvider nodeProvider;
    private final Mesh mesh = new Mesh();
    private final ScheduledExecutorService exchangeScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService mergeScheduler = Executors.newSingleThreadScheduledExecutor();
    @Value("${s34j.service-mesh.name:default}")
    private String name;
    @Autowired
    private MeshTemplate meshTemplate;
    @Value("${s34j.service-mesh.deleteAfterInactiveMinutes:5}")
    private int deleteAfterInactiveMinutes;
    private int deleteAfterInactiveMillis;
    @Value("${s34j.service-mesh.exchangeIntervalSeconds:20}")
    private int exchangeIntervalSeconds;
    @Value("${s34j.service-mesh.cleanIntervalSeconds:60}")
    private int cleanIntervalSeconds;
    @Value("${s34j.service-mesh.initialHostExchangeCount:1}")
    private int initialHostExchangeCount;
    @Value("${s34j.service-mesh.eldestExchangeCount:1}")
    private int eldestHostExchangeCount;
    @Value("${s34j.service-mesh.randomExchangeCount:1}")
    private int randomExchangeCount;

    @PostConstruct
    void start() {
        mesh.setName(name);
        mesh.setNodes(new ConcurrentHashMap<>());
        deleteAfterInactiveMillis = deleteAfterInactiveMinutes * 60 * 1000;
        exchangeScheduler.schedule(this::initialExchange, exchangeIntervalSeconds / 2, TimeUnit.SECONDS);
    }

    private void initialExchange() {
        log.info("Mesh manager stated.");
        log.info("- Mesh name: {}", name);
        log.info("- Current node: {}", nodeProvider.provide());
        log.info("- Initial hosts: {}", hostsProvider.provide());
        log.info("- Exchange interval is {} seconds.", exchangeIntervalSeconds);
        log.info("- Node will be deleted if inactive for {} minutes.", deleteAfterInactiveMinutes);

        List<Exchange> initialExchanges = hostsProvider.provide().parallelStream()
                .map(this::exchangeWithHost)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        initialExchanges.forEach(this::doMerge);

        exchangeScheduler.scheduleAtFixedRate(this::exchange,
                exchangeIntervalSeconds, exchangeIntervalSeconds, TimeUnit.SECONDS);
        mergeScheduler.scheduleAtFixedRate(this::cleanUp,
                cleanIntervalSeconds, cleanIntervalSeconds, TimeUnit.SECONDS);
    }

    private void cleanUp() {
        List<String> nodesToDelete = mesh.getNodes().values().stream()
                .filter((node) -> System.currentTimeMillis() - node.getUpdated() > deleteAfterInactiveMillis)
                .map(Node::getName)
                .collect(Collectors.toList());
        if (nodesToDelete.size() > 0) {
            log.info("Cleaning up {}", nodesToDelete);
            nodesToDelete.forEach(mesh.getNodes()::remove);
            nodesToDelete.forEach(knownLatencies::invalidate);
        }
    }

    /**
     * Each steps a node do exchange with 1 initial host, 1 random host and 1 oldest updated host
     */
    private void exchange() {
        if (!activeProvider.provide()) {
            log.info("Skipped exchange since host is inactive.");
            return;
        }
        Node currentNode = nodeProvider.provide();

        //exchange with 1 initial host
        List<String> initialHosts = new ArrayList<>(hostsProvider.provide());
        Collections.shuffle(initialHosts);
        List<String> initialExchanged = initialHosts.stream()
                .filter((host) -> !StringUtils.equalsIgnoreCase(host, currentNode.getUrl()))
                .map(this::exchangeWithHost)
                .filter(Objects::nonNull)
                .peek((received) -> mergeScheduler.submit(() -> doMerge(received)))
                .map(exchange -> exchange.getCurrent().getName())
                .limit(initialHostExchangeCount)
                .collect(Collectors.toList());


        //exchange with 1 least updated host
        List<Node> nodes = new ArrayList<>(mesh.getNodes().values());
        List<String> eldestExchanged = nodes.stream()
                .filter(Node::isActive)
                .filter((n) -> !StringUtils.equalsIgnoreCase(n.getUrl(), currentNode.getUrl()))
                .filter((n) -> !initialExchanged.contains(n.getName()))
                .sorted((n1, n2) -> (int) (n1.getUpdated() - n2.getUpdated()))
                .map((node) -> exchangeWithHost(node.getUrl()))
                .filter(Objects::nonNull)
                .peek((received) -> mergeScheduler.submit(() -> doMerge(received)))
                .map(exchange -> exchange.getCurrent().getName())
                .limit(eldestHostExchangeCount)
                .collect(Collectors.toList());

        //exchange with 1 random host
        Collections.shuffle(nodes);
        List<String> randomExchanged = nodes.stream()
                .filter(Node::isActive)
                .filter((n) -> !StringUtils.equalsIgnoreCase(n.getUrl(), currentNode.getUrl()))
                .filter((n) -> !initialExchanged.contains(n.getName()))
                .filter((n) -> !eldestExchanged.contains(n.getName()))
                .map((node) -> exchangeWithHost(node.getUrl()))
                .filter(Objects::nonNull)
                .peek((received) -> mergeScheduler.submit(() -> doMerge(received)))
                .map(exchange -> exchange.getCurrent().getName())
                .limit(randomExchangeCount)
                .collect(Collectors.toList());

        log.debug("initialExchanged={}", initialExchanged);
        log.debug("eldestExchanged={}", eldestExchanged);
        log.debug("randomExchanged={}", randomExchanged);
    }

    private Exchange exchangeWithHost(String url) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Exchange received = meshTemplate.post(url, MeshFilter.SM_EXCHANGE, getExchange(), Exchange.class);
        long latency = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        log.debug("Exchanged with {} in {}ms. received={}", url, latency, received);
        if (received != null) {
            exchangeLatencies.put(received.getCurrent().getName(), (int) latency);
        }
        return received;
    }

    public Exchange getExchange() {
        Node current = nodeProvider.provide();
        mesh.getNodes().put(current.getName(), current);
        exchangeLatencies.put(current.getName(), 0);

        HashMap<String, Node> exchangeNodes = new HashMap<>();

        mesh.getNodes().values().stream()
                .filter((n) -> Math.abs(System.currentTimeMillis() - n.getUpdated()) < deleteAfterInactiveMillis)
                .forEach((n) -> exchangeNodes.put(n.getName(), n));

        Mesh exchangeMesh = new Mesh();
        exchangeMesh.setName(mesh.getName());
        exchangeMesh.setNodes(exchangeNodes);

        Exchange exchange = new Exchange();
        exchange.setCurrent(current);
        exchange.setMesh(exchangeMesh);
        exchange.setLatencies(exchangeLatencies.asMap());
        return exchange;
    }

    @PreDestroy
    public void stop() {
        exchangeScheduler.shutdown();
        mergeScheduler.shutdown();
    }

    public Exchange merge(Exchange received) {
        //queue merge here
        mergeScheduler.submit(() -> this.doMerge(received));
        return getExchange();
    }

    private void doMerge(Exchange exchange) {
        if (exchange == null || exchange.getCurrent() == null || exchange.getMesh() == null) {
            log.warn("Exchange is null. exchange={}", exchange);
            return;
        }
        if (!equalsIgnoreCase(exchange.getMesh().getName(), mesh.getName())) {
            log.error("Wrong mesh name. exchange={}, name={}", exchange.getMesh().getName(), mesh.getName());
            return;
        }

        Node current = nodeProvider.provide();
        Node node = exchange.getCurrent();
        if (equalsIgnoreCase(node.getName(), current.getName())) {
            log.warn("Exchanging with current node or duplicated node name: {}", node.getName());
            return;
        }

        Map<String, Node> nodes = mesh.getNodes();
        exchange.getMesh().getNodes().forEach((k, v) -> {
            if (equalsIgnoreCase(k, current.getName())) {
                //handle current node
                nodes.put(current.getName(), current);
            } else if (equalsIgnoreCase(k, node.getName())) {
                //handle exchanging node
                nodes.put(node.getName(), node);
            } else {
                //merge based on last updated
                Node knownNode = nodes.get(k);
                if (knownNode == null || knownNode.getUpdated() < v.getUpdated()) {
                    nodes.put(k, v);
                }
            }
        });
        //reset again to make sure it is correctly updated
        nodes.put(current.getName(), current);
        nodes.put(node.getName(), node);

        lastExchanges.put(node.getName(), System.currentTimeMillis());
        knownLatencies.put(node.getName(), exchange.getLatencies());
        knownLatencies.put(current.getName(), getExchangeLatencies());
//        printMeshInfo();
    }

    private void printMeshInfo() {
        try {
            ObjectMapper debugMapper = new ObjectMapper();
            debugMapper.enable(SerializationFeature.INDENT_OUTPUT);
            log.info("Updated mesh:{}", debugMapper.writeValueAsString(mesh));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Map<String, Integer> getExchangeLatencies() {
        return exchangeLatencies.asMap();
    }

    public Map<String, Map<String, Integer>> getKnownLatencies() {
        return knownLatencies.asMap();
    }
}
