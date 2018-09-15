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
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class MeshPool {

    private ScheduledExecutorService pool;

    @Value("${s34j.service-mesh.poolSize:0}")
    private int poolSize;


    @PostConstruct
    public void start() {
        poolSize = poolSize > 0 ? poolSize : Runtime.getRuntime().availableProcessors() - 1;
        poolSize = poolSize > 0 ? poolSize : 1;
        pool = Executors.newScheduledThreadPool(poolSize);
    }

    @PreDestroy
    public void stop() {
        pool.shutdown();
    }
}
