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

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Method annotation to signal GooglePubSub to invoke this method when a message arrives.
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(PubSubListeners.class)
public @interface PubSubListener {
    /**
     * @return Topic name to be created in google pub sub. Default to message class name
     */
    @AliasFor("topic")
    String value() default "";

    /**
     *
     * @return Topic name to be created in google pub sub. Default to message class name
     */
    @AliasFor("value")
    String topic() default "";

    /**
     *
     * @return Subscriber name to be created
     */
    String name() default "";

    /**
     *
     * @return true if an unique suffix will be added to subscriber name.
     */
    boolean addUniqueSuffix() default false;

}
