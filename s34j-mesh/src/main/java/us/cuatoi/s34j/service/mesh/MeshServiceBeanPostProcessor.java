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

import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class MeshServiceBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, ServiceMethodBeanHolder> services = new ConcurrentHashMap<>();

    public Set<String> getServices() {
        return ImmutableSet.copyOf(services.keySet());
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);
        Map<Method, Set<MeshServiceHandler>> annotatedMethods = MethodIntrospector.selectMethods(beanClass,
                (MethodIntrospector.MetadataLookup<Set<MeshServiceHandler>>) method -> {
                    Set<MeshServiceHandler> listenerMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                            method, MeshServiceHandler.class, MeshServiceHandlers.class);
                    return (!listenerMethods.isEmpty() ? listenerMethods : null);
                });
        if (annotatedMethods.size() == 0) return bean;
        for (Map.Entry<Method, Set<MeshServiceHandler>> entry : annotatedMethods.entrySet()) {
            Method method = entry.getKey();
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException("Invalid MeshServiceHandler method. Method be public. " +
                        "Class=" + beanClass + ", method=" + method);
            }
            if (method.getParameters().length != 1) {
                throw new IllegalArgumentException("Invalid MeshServiceHandler method. Method must have only 1 argument. " +
                        "Class=" + beanClass + ", method=" + method);
            }
            for (MeshServiceHandler listener : entry.getValue()) {
                String service = listener.name();
                service = isNotBlank(service) ? service : beanName;
                ServiceMethodBeanHolder holder = new ServiceMethodBeanHolder();
                holder.setService(service);
                holder.setBean(bean);
                holder.setMethod(method);
                services.put(service, holder);
            }
        }

        return bean;
    }

    @Data
    static class ServiceMethodBeanHolder {
        String service;
        Object bean;
        Method method;
    }
}
