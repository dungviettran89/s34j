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
import java.util.Map;

/**
 * Indicate a service invocation
 */
@Data
public class Invoke {
    /**
     * Service to invoke
     */
    String service;
    /**
     * Input of the service
     */
    String inputJson;
    /**
     * Output of the service
     */
    String outputJson;

    /**
     * Error if there is an exception
     */
    String error;

    /**
     * Node started in invocation
     */
    String from;

    /**
     * Target of this invocation, can be null
     */
    String to;

    /**
     * Invocation chain, in case it passed through more than 1 host
     */
    List<String> chain;

    /**
     * Used to match response
     */
    String correlationId;

    /**
     * Header for this invocation
     */
    Map<String, String> headers;
}
