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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static us.cuatoi.s34j.service.mesh.MeshFilter.SM_AUTHORIZATION;
import static us.cuatoi.s34j.service.mesh.MeshFilter.SM_DATE;

@Slf4j
public class MeshTemplate {
    @Value("${s34j.service-mesh.name:default}")
    private String name;
    @Value("${s34j.service-mesh.secret:nx22HgZxTSfcWT2YnYFr4CAnyt7dffTbXbNRNl5y}")
    private String secret;
    private ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate = new RestTemplate();


    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <R> R post(String url, String meshMethod, Object entity, Class<R> responseClass) {
        try {
            String json = toJson(entity);
            String time = String.valueOf(System.currentTimeMillis());
            String authorization = calculateAuthorization(json, time);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.set(MeshFilter.SM_DATE, time);
            requestHeaders.set(MeshFilter.SM_AUTHORIZATION, authorization);
            requestHeaders.set(MeshFilter.SM_METHOD, meshMethod);
            HttpEntity<String> request = new HttpEntity<>(json, requestHeaders);
            ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.POST, request, responseClass);

            String exchangeDate = extractFirstHeader(response, SM_DATE);
            String exchangeAuthorization = extractFirstHeader(response, SM_AUTHORIZATION);
            R object = response.getBody();

            if (!validExchange(toJson(object), exchangeDate, exchangeAuthorization)) {
                return null;
            } else {
                return object;
            }
        } catch (Exception e) {
            log.warn("Can not post error={}, url={}, entity={}, responseClass={}",
                    e.getMessage(), url, entity, responseClass);
            return null;
        }
    }

    public <R> String toJson(R object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <R> R fromJson(String json, Class<R> rClass)  {
        try {
            return objectMapper.readValue(json, rClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <R> boolean validExchange(String json, String exchangeDate, String exchangeAuthorization) {
        if (isAnyBlank(exchangeDate, exchangeAuthorization)) {
            log.warn("Header malformed. responseDate={}, exchangeAuthorization={}",
                    exchangeDate, exchangeAuthorization);
            return false;
        }
        if (Math.abs(System.currentTimeMillis() - Long.parseLong(exchangeDate)) > 10 * 60 * 1000) {
            log.warn("Exchange time to skew. responseDate={}, currentDate={}",
                    exchangeDate, System.currentTimeMillis());
            return false;
        }
        String calculatedResponseAuthorization = calculateAuthorization(json, exchangeDate);
        if (!equalsIgnoreCase(exchangeAuthorization, calculatedResponseAuthorization)) {
            log.warn("Invalid authorization header. exchangeAuthorization={}, calculatedResponseAuthorization={}",
                    exchangeAuthorization, calculatedResponseAuthorization);
            return false;
        }
        return true;
    }

    public String calculateAuthorization(String json, String time) {
        String hash = Hashing.hmacSha256(secret.getBytes(UTF_8))
                .hashString(name, UTF_8)
                .toString();
        hash = Hashing.hmacSha256(hash.getBytes(UTF_8))
                .hashString(time, UTF_8)
                .toString();
        return Hashing.hmacSha256(hash.getBytes(UTF_8))
                .hashString(json, UTF_8)
                .toString();
    }

    private <R> String extractFirstHeader(ResponseEntity<R> response, String name) {
        List<String> headers = response.getHeaders().get(name);
        return headers != null ? headers.get(0) : "";
    }
}
