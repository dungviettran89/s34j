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

import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;
import us.cuatoi.s34j.service.mesh.bo.Exchange;
import us.cuatoi.s34j.service.mesh.bo.Invoke;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;

@Slf4j
public class MeshFilter extends OncePerRequestFilter {

    public static final String SM_AUTHORIZATION = "x-sm-authorization";
    public static final String SM_DATE = "x-sm-date";
    public static final String SM_METHOD = "x-sm-method";
    public static final String SM_DIRECT_INVOKE = "direct-invoke";
    public static final String SM_FORWARD_INVOKE = "forward-invoke";
    public static final String SM_EXCHANGE = "exchange";

    @Autowired
    private MeshTemplate meshTemplate;
    @Autowired
    private MeshManager meshManager;
    @Autowired
    private MeshInvoker meshInvoker;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isAnyBlank(request.getHeader(SM_AUTHORIZATION),
                request.getHeader(SM_AUTHORIZATION),
                request.getHeader(SM_METHOD))) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader(SM_AUTHORIZATION);
        String date = request.getHeader(SM_DATE);
        String method = request.getHeader(SM_METHOD);
        String json = CharStreams.toString(new InputStreamReader(request.getInputStream(), UTF_8));
        if (!meshTemplate.validExchange(json, date, authorization)) {
            log.warn("Invalid exchange. authorization={} date={} method={}", authorization, date, method);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid exchange");
        }
        switch (method) {
            case SM_DIRECT_INVOKE:
                doDirectInvoke(response, json);
                return;
            case SM_EXCHANGE:
                doExchange(response, json);
                return;
            default:
                log.warn("Unknown method, method={}", method);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown method");
        }
    }

    private void doExchange(HttpServletResponse response, String json) throws IOException {
        Exchange received = meshTemplate.fromJson(json, Exchange.class);
        Exchange exchange = meshManager.merge(received);
        String responseJson = meshTemplate.toJson(exchange);
        String responseDate = String.valueOf(System.currentTimeMillis());
        String responseAuthorization = meshTemplate.calculateAuthorization(responseJson, responseDate);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(SM_DATE, responseDate);
        response.setHeader(SM_AUTHORIZATION, responseAuthorization);
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(responseJson);
        log.debug("Exchange request completed. received={}", received);
    }

    private void doDirectInvoke(HttpServletResponse response, String json) throws IOException {
        Invoke invoke = meshTemplate.fromJson(json, Invoke.class);
        Invoke invokeResult = meshInvoker.handleDirectInvoke(invoke);
        String responseJson = meshTemplate.toJson(invokeResult);
        String responseDate = String.valueOf(System.currentTimeMillis());
        String responseAuthorization = meshTemplate.calculateAuthorization(responseJson, responseDate);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(SM_DATE, responseDate);
        response.setHeader(SM_AUTHORIZATION, responseAuthorization);
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(responseJson);
        log.debug("Direct invoke completed. service={}", invoke.getService());
    }
}
