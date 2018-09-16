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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Slf4j
public class MeshManager {

    @Autowired
    private ActiveProvider activeProvider;
    @Autowired
    private HostsProvider hostsProvider;
    @Autowired
    private NodeProvider nodeProvider;
    private final Cache<String, Long> lastExchanges = CacheBuilder.newBuilder().build();
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
    @Value("${s34j.service-mesh.exchangeIntervalSeconds:5}")
    private int exchangeIntervalSeconds;

    @PostConstruct
    public void start() {
        deleteAfterInactiveMillis = deleteAfterInactiveMinutes * 60 * 1000;
        exchangeScheduler.schedule(this::initialExchange, 2, TimeUnit.SECONDS);
    }

    private void initialExchange() {
        log.debug("Mesh manager stated.");
        log.debug("- Mesh name: {}", name);
        log.debug("- Current node: {}", nodeProvider.provide());
        log.debug("- Initial hosts: {}", hostsProvider.provide());
        log.debug("- Exchange interval is {} seconds.", exchangeIntervalSeconds);
        log.debug("- Node will be deleted if inactive for {} minutes.", deleteAfterInactiveMinutes);

        Node currentNode = nodeProvider.provide();
        ConcurrentHashMap<String, Node> nodes = new ConcurrentHashMap<>();
        nodes.put(currentNode.getName(), currentNode);
        mesh.setName(name);
        mesh.setNodes(nodes);

        List<Exchange> initialExchanges = hostsProvider.provide().parallelStream()
                .map(this::exchangeWithHost)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        initialExchanges.forEach(this::doMerge);
    }

    private Exchange exchangeWithHost(String url) {
        Exchange received = meshTemplate.post(url, MeshFilter.SM_EXCHANGE, getExchange(), Exchange.class);
        log.info("Exchanged with {}. Valid={}", url, received != null);
        log.debug("received={}", received);
        return received;
    }

    public Exchange getExchange() {
        HashMap<String, Node> exchangeNodes = new HashMap<>();

        mesh.getNodes().values().stream()
                .filter((n) -> Math.abs(System.currentTimeMillis() - n.getUpdated()) < deleteAfterInactiveMillis)
                .forEach((n) -> exchangeNodes.put(n.getName(), n));

        Mesh exchangeMesh = new Mesh();
        exchangeMesh.setName(mesh.getName());
        exchangeMesh.setNodes(exchangeNodes);

        Exchange exchange = new Exchange();
        exchange.setCurrent(nodeProvider.provide());
        exchange.setMesh(exchangeMesh);
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
        printMeshInfo();
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
}
