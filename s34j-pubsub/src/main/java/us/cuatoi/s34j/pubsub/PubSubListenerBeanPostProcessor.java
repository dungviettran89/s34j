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

/**
 * Perform all necessary handling to use PubSubListener annotation.
 */
public class PubSubListenerBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PubSubListenerBeanPostProcessor.class);

    @Autowired
    private PubSub pubSub;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);
        Map<Method, Set<PubSubListener>> annotatedMethods = MethodIntrospector.selectMethods(beanClass,
                (MethodIntrospector.MetadataLookup<Set<PubSubListener>>) method -> {
                    Set<PubSubListener> listenerMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                            method, PubSubListener.class, PubSubListeners.class);
                    return (!listenerMethods.isEmpty() ? listenerMethods : null);
                });
        if (annotatedMethods.size() == 0) return bean;
        for (Map.Entry<Method, Set<PubSubListener>> entry : annotatedMethods.entrySet()) {
            Method method = entry.getKey();
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException("Invalid PubSubListener method. Method be public. " +
                        "Class=" + beanClass + ", method=" + method);
            }
            if (method.getParameters().length != 1) {
                throw new IllegalArgumentException("Invalid PubSubListener method. Method must have only 1 argument. " +
                        "Class=" + beanClass + ", method=" + method);
            }
            Class<?> messageClass = method.getParameters()[0].getType();
            for (PubSubListener listener : entry.getValue()) {
                String topic = listener.topic();
                topic = isEmpty(topic) ? messageClass.getName() : topic;
                String subscription = listener.name();
                subscription = isEmpty(subscription) ? (beanName + "." + method.getName()) : subscription;
                if (listener.addUniqueSuffix()) {
                    subscription += "." + UUID.randomUUID().toString();
                }
                pubSub.register(topic, subscription, messageClass, (message) -> {
                    try {
                        method.invoke(bean, message.getPayload());
                    } catch (IllegalAccessException | InvocationTargetException invocationError) {
                        logger.error("Can not invoke method. beanClass={} beanName={} method={}  message={}",
                                beanClass, beanName, method, message, invocationError);
                        throw new RuntimeException(invocationError);
                    }
                });
                logger.info("Registered topic={} subscription={} beanName={} method={}",
                        topic, subscription, beanName, method);
            }
        }
        return bean;
    }
}
