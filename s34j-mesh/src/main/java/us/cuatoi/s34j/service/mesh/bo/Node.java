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

package us.cuatoi.s34j.service.mesh.bo;

import lombok.Data;

import java.util.List;

/**
 * Information about this node
 */
@Data
public class Node {
    /**
     * Name of the node
     */
    private String name;
    /**
     * Url of the node
     */
    private String url;
    /**
     * Available service on the node
     */
    private List<String> services;
    /**
     * Mark if this node can accept request
     */
    private boolean active;
    /**
     * Last updated or last heart beat
     */
    private long updated;

    /**
     * Total CPU on this node
     */
    private int cpu;
    /**
     * Current unix load
     */
    private float load;
    /**
     * Total ram available to this JVM
     */
    private long totalRam;
    /**
     * Total ram used in this JVM
     */
    private long usedRam;
}
