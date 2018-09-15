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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import us.cuatoi.s34j.service.mesh.providers.ActiveProvider;
import us.cuatoi.s34j.service.mesh.providers.HostsProvider;
import us.cuatoi.s34j.service.mesh.providers.NodeProvider;

import javax.annotation.PostConstruct;

@Slf4j
public class MeshManager {
    @Autowired
    private MeshPool meshPool;
    @Autowired
    private ActiveProvider activeProvider;
    @Autowired
    private HostsProvider hostsProvider;
    @Autowired
    private NodeProvider nodeProvider;

    @PostConstruct
    public void start() {
        log.debug("Mesh manager starting.");
        log.debug("- Initial hosts: {}", hostsProvider.provide());
        log.debug("- Current node: {}", nodeProvider.provide());
    }
}
