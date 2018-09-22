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
import us.cuatoi.s34j.service.mesh.bo.Latencies;
import us.cuatoi.s34j.service.mesh.bo.Mesh;
import us.cuatoi.s34j.service.mesh.bo.Node;
import us.cuatoi.s34j.service.mesh.providers.NodeProvider;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

/**
 * Provide some default handler to get mesh information
 */
public class DefaultMeshHandlers {
    @Autowired
    private NodeProvider nodeProvider;
    @Autowired
    private MeshManager meshManager;

    /**
     * Provide node information
     *
     * @param name of the node
     * @return node information including: services, name, url
     */
    @MeshServiceHandler("getNodeInfo")
    public Node getNodeInfo(String name) {
        Node currentNode = nodeProvider.provide();
        return equalsIgnoreCase(name, currentNode.getName()) ? currentNode : null;
    }

    /**
     * Provide mesh information
     *
     * @param name of the mesh
     * @return all mesh information
     */
    @MeshServiceHandler("getMeshInfo")
    public Mesh getMeshInfo(String name) {
        Mesh currentMesh = meshManager.getMesh();
        return equalsIgnoreCase(name, currentMesh.getName()) ? currentMesh : null;
    }

    /**
     * Provide latencies information in the node
     *
     * @param name of the node
     * @return known latencies and exchange latencies
     */
    @MeshServiceHandler("getLatencies")
    public Latencies getLatencies(String name) {
        Node currentNode = nodeProvider.provide();
        if (!equalsIgnoreCase(name, currentNode.getName())) return null;
        Latencies latencies = new Latencies();
        latencies.setExchangeLatencies(meshManager.getExchangeLatencies());
        latencies.setKnownLatencies(meshManager.getKnownLatencies());
        return latencies;
    }
}
