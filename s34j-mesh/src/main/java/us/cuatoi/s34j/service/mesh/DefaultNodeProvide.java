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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import us.cuatoi.s34j.service.mesh.bo.Node;
import us.cuatoi.s34j.service.mesh.providers.ActiveProvider;
import us.cuatoi.s34j.service.mesh.providers.NodeProvider;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DefaultNodeProvide implements NodeProvider {

    @Autowired
    private ActiveProvider activeProvider;
    @Value("${server.address:}")
    private String address;
    @Value("${server.port}")
    private int port;
    @Value("${server.ssl.key-alias:}")
    private String sslKeyAlias;
    @Value("${s34j.service-mesh.name:}")
    private String name;
    @Value("${s34j.service-mesh.url:}")
    private String url;
    @Autowired
    private MeshServiceBeanPostProcessor meshServiceBeanPostProcessor;

    @Override
    public Node provide() {
        String url = this.url;
        url = isNotBlank(url) ? url : detectUrl();

        String name = this.name;
        name = isNotBlank(name) ? name : url;

        double loadAverage = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        loadAverage = loadAverage > 0 ? loadAverage : 0;

        Node node = new Node();
        node.setName(url);
        node.setUrl(name);
        node.setServices(meshServiceBeanPostProcessor.getServices());
        node.setActive(activeProvider.provide());
        Runtime runtime = Runtime.getRuntime();
        node.setCpu(runtime.availableProcessors());
        node.setLoad(loadAverage);
        node.setTotalMemory(runtime.totalMemory());
        node.setFreeMemory(runtime.freeMemory());
        node.setUpdated(System.currentTimeMillis());
        return node;
    }

    private String detectUrl() {
        try {
            String protocol = isNotBlank(sslKeyAlias) ? "https://" : "http://";
            String address = isNotBlank(this.address) ? this.address : InetAddress.getLocalHost().getHostAddress();
            return protocol + address + ":" + port;
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }
}
