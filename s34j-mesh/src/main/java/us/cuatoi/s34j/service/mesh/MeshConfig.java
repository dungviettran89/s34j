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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import us.cuatoi.s34j.service.mesh.providers.ActiveProvider;
import us.cuatoi.s34j.service.mesh.providers.HostsProvider;
import us.cuatoi.s34j.service.mesh.providers.NodeProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

public class MeshConfig {

    @Bean
    public MeshServiceBeanPostProcessor meshServiceBeanPostProcessor() {
        return new MeshServiceBeanPostProcessor();
    }

    @Bean
    public MeshManager meshManager() {
        return new MeshManager();
    }

    @Bean
    public MeshInvoker meshInvoker() {
        return new MeshInvoker();
    }

    @Bean
    public DefaultMeshHandlers defaultHandlers() {
        return new DefaultMeshHandlers();
    }

    @Bean
    public FilterRegistrationBean<MeshFilter> meshFilterRegistration(MeshFilter meshFilter) {
        FilterRegistrationBean<MeshFilter> registrationBean = new FilterRegistrationBean<>(meshFilter);
        registrationBean.setOrder(HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    @Bean
    public MeshFilter meshFilter() {
        return new MeshFilter();
    }

    @Bean
    @ConditionalOnMissingBean(ActiveProvider.class)
    public ActiveProvider activeProvider(@Value("${s34j.service-mesh.active:true}") boolean active) {
        return () -> active;
    }

    @Bean
    @ConditionalOnMissingBean(HostsProvider.class)
    public HostsProvider hostsProvider(@Value("${s34j.service-mesh.hosts}") String hosts) {
        List<String> list = Arrays.stream(hosts.split(","))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (list.isEmpty()) throw new IllegalArgumentException("Please provide an initial list of host.");
        return () -> list;
    }

    @Bean
    @ConditionalOnMissingBean(NodeProvider.class)
    public NodeProvider currentNodeProvider() {
        return new DefaultNodeProvider();
    }

    @Bean
    @ConditionalOnMissingBean(MeshTemplate.class)
    public MeshTemplate meshTemplate() {
        return new MeshTemplate();
    }


}
