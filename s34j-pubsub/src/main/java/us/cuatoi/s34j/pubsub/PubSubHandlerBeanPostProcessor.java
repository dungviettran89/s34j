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

package us.cuatoi.s34j.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.util.StringUtils.isEmpty;

public class PubSubHandlerBeanPostProcessor extends PubSubBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PubSubHandlerBeanPostProcessor.class);

    @Autowired
    private PubSub pubSub;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);
        Map<Method, Set<PubSubHandler>> annotatedMethods = MethodIntrospector.selectMethods(beanClass,
                (MethodIntrospector.MetadataLookup<Set<PubSubHandler>>) method -> {
                    Set<PubSubHandler> listenerMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                            method, PubSubHandler.class, PubSubHandlers.class);
                    return (!listenerMethods.isEmpty() ? listenerMethods : null);
                });
        if (annotatedMethods.size() == 0) return bean;
        for (Map.Entry<Method, Set<PubSubHandler>> entry : annotatedMethods.entrySet()) {
            Method method = entry.getKey();
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException("Invalid PubSubHandler method. Method be public. " +
                        "Class=" + beanClass + ", method=" + method);
            }
            if (method.getParameters().length != 1) {
                throw new IllegalArgumentException("Invalid PubSubHandler method. Method must have only 1 argument. " +
                        "Class=" + beanClass + ", method=" + method);
            }
            if (method.getReturnType() == null || method.getReturnType().equals(Void.TYPE)) {
                throw new IllegalArgumentException("Invalid PubSubHandler method. Method must not return void. " +
                        "Class=" + beanClass + ", method=" + method);
            }
            Class<?> requestClass = method.getParameters()[0].getType();
            Class<?> responseClass = method.getReturnType();
            for (PubSubHandler listener : entry.getValue()) {
                String requestTopic = listener.requestTopic();
                requestTopic = isEmpty(requestTopic) ? requestClass.getName() : requestTopic;
                String subscription = listener.name();
                subscription = isEmpty(subscription) ? beanName + "." + method.getName() : subscription;
                if (listener.addUniqueSuffix()) {
                    subscription += "." + UUID.randomUUID().toString();
                }
                pubSub.register(requestTopic, subscription, requestClass, (message) -> {
                    try {
                        String responseTopic = listener.responseTopic();
                        responseTopic = isEmpty(responseTopic) ? responseClass.getName() : responseTopic;
                        Object response = method.invoke(bean, message);
                        pubSub.publish(responseTopic, response);
                    } catch (IllegalAccessException | InvocationTargetException invocationError) {
                        logger.error("Can not invoke method. beanClass={} beanName={} method={}  message={}",
                                beanClass, beanName, method, message, invocationError);
                        throw new RuntimeException(invocationError);
                    }
                });
                logger.info("Registered requestTopic={} subscription={} beanName={} method={}",
                        requestTopic, subscription, beanName, method);
            }
        }
        return bean;
    }
}
