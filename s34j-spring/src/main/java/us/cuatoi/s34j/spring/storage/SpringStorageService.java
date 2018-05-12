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

package us.cuatoi.s34j.spring.storage;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.auth.AuthenticationRule;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.dto.ErrorResponseXml;
import us.cuatoi.s34j.spring.helper.Headers;
import us.cuatoi.s34j.spring.helper.InputStreamWrapper;
import us.cuatoi.s34j.spring.helper.SplitOutputStream;
import us.cuatoi.s34j.spring.operation.ExecutionRule;
import us.cuatoi.s34j.spring.operation.bucket.BucketVerifier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34j.spring.SpringStorageConstants.*;

@Service
public class SpringStorageService {
    private static final Logger logger = LoggerFactory.getLogger(SpringStorageService.class);
    @Value("${s34j.spring.blockSizeBytes:6291456}")//6MB
    private int blockSizeBytes;
    @Autowired
    private List<AuthenticationRule> authenticationRules;
    @Autowired
    private List<ExecutionRule> executionRules;


    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(response);

        String requestId = UUID.randomUUID().toString();
        String serverId = "springStorageService";
        Facts facts = new Facts();
        try {
            //1: gather information
            String method = request.getMethod();
            facts.put("requestId", requestId);
            facts.put("serverId", serverId);
            facts.put("method", method);
            facts.put("requestInputStream", request.getInputStream());
            facts.put(method, true);
            String path = StringUtils.trimToEmpty(request.getPathInfo());
            logger.info("handle(): path=" + path);
            facts.put("path", path);
            String bucketName = path.substring(1);
            logger.info("handle(): bucketName=" + bucketName);
            if (bucketName.length() > 0) {
                int firstSlashIndex = bucketName.indexOf('/');
                if (firstSlashIndex > 0) {
                    bucketName = bucketName.substring(0, firstSlashIndex);
                    String objectName = path.substring(firstSlashIndex + 2);
                    facts.put("objectName", objectName);
                    logger.info("handle(): objectName=" + objectName);
                }
                facts.put("bucketName", bucketName);
                BucketVerifier.verifyBucketName(bucketName);
            }
            Collections.list(request.getHeaderNames())
                    .forEach((h) -> {
                        logger.debug("Header: " + h + "=" + request.getHeader(h));
                        facts.put("header:" + h, request.getHeader(h));
                    });

            Map<String, String> queryParameters = new HashMap<>();
            String url = request.getRequestURL().toString();
            if (isNotBlank(request.getQueryString())) {
                url += "?" + request.getQueryString();
            }
            facts.put("url", url);
            URLEncodedUtils.parse(new URI(url), UTF_8)
                    .forEach((pair) -> {
                        logger.debug("Query: " + pair.getName() + "=" + pair.getValue());
                        queryParameters.put(pair.getName(), pair.getValue());
                        facts.put("query:" + pair.getName(), pair.getValue());
                    });
            Collections.list(request.getParameterNames()).stream()
                    .filter(key -> !queryParameters.containsKey(key))
                    .forEach((p) -> {
                        logger.debug("Form: " + p + "=" + request.getParameter(p));
                        facts.put("form:" + p, request.getParameter(p));
                    });

            //2: perform authentication
            Rules authentication = new Rules();
            authenticationRules.forEach(authentication::register);
            RulesEngine authenticationEngine = new DefaultRulesEngine();
            authenticationEngine.getParameters().setSkipOnFirstAppliedRule(true);
            authenticationEngine.fire(authentication, facts);
            if (!TRUE.equals(facts.get("authenticated"))) {
                ErrorCode errorCode = facts.get("errorCode");
                if (errorCode == null) {
                    errorCode = ErrorCode.ACCESS_DENIED;
                }
                writeError(response, facts, errorCode);
                return;
            }
            //3: parse payload
            if (!equalsAnyIgnoreCase(method, "GET")) {
                InputStream sourceStream = facts.get("requestInputStream");
                //verify md5
                final String md5 = facts.get("header:content-md5");
                boolean md5Enabled = isNotBlank(md5);
                if (md5Enabled) {
                    logger.info("handle() md5Enabled=" + md5Enabled);
                    sourceStream = verifyContentMd5(sourceStream, md5);
                }
                //parse streaming payload

                //verify sha256
                String sha256 = facts.get("header:x-amz-content-sha256");
                boolean sha256Enabled = isNotBlank(sha256) &&
                        !equalsAny(sha256, STREAMING_PAYLOAD, UNSIGNED_PAYLOAD, BLANK_PAYLOAD);
                if (sha256Enabled) {
                    logger.info("handle() sha256Enabled=" + sha256Enabled);
                    sourceStream = verifyContentSha256(sourceStream, sha256);
                }

                HashingInputStream eTagStream = new HashingInputStream(Hashing.goodFastHash(128), sourceStream);
                sourceStream = eTagStream;
                //3.5 parse stream
                try (InputStream is = sourceStream) {
                    SplitOutputStream splitOutputStream = new SplitOutputStream(blockSizeBytes);
                    try (SplitOutputStream os = splitOutputStream) {
                        long length = ByteStreams.copy(is, os);
                        facts.put("contentLength", length);
                    }
                    facts.put("parts", splitOutputStream.getInputStreams());
                }
                facts.put("ETag", eTagStream.hash().toString());
            }
            //4: perform execution
            Rules execution = new Rules();
            executionRules.forEach(execution::register);
            RulesEngine executionEngine = new DefaultRulesEngine();
            executionEngine.getParameters().setSkipOnFirstAppliedRule(true);
            executionEngine.fire(execution, facts);

            //4: response
            ErrorCode errorCode = facts.get("errorCode");
            if (errorCode != null) {
                writeError(response, facts, errorCode);
                return;
            }

            Integer statusCode = facts.get("statusCode");
            if (statusCode != null) {
                writeResponse(response, facts, statusCode);
                return;
            }

            writeError(response, facts, ErrorCode.NOT_IMPLEMENTED);
        } catch (SpringStorageException storageException) {
            writeError(response, facts, storageException.getErrorCode());
        } catch (Exception unexpectedException) {
            logger.error("handle() unexpectedException" + unexpectedException, unexpectedException);
            ErrorCode error = ErrorCode.INTERNAL_ERROR;
            Throwable rootCause = ExceptionUtils.getRootCause(unexpectedException);
            if (rootCause instanceof SpringStorageException) {
                error = ((SpringStorageException) rootCause).getErrorCode();
            }
            writeError(response, facts, error);
        } finally {
            //clean up
            List<InputStream> partsToCleanUp = facts.get("parts");
            if (partsToCleanUp != null) {
                logger.info("handle() partsToCleanUp.size=" + partsToCleanUp.size());
                partsToCleanUp.forEach(Closeables::closeQuietly);
            }
        }
    }

    private void writeResponse(HttpServletResponse servletResponse, Facts facts, Integer statusCode) throws IOException {
        logger.info("writeResponse() statusCode=" + statusCode);
        logger.info("writeResponse() contentType=" + facts.get("contentType"));
        logger.info("writeResponse() requestId=" + facts.get("requestId"));
        logger.info("writeResponse() response=" + facts.get("response"));
        logger.info("writeResponse() objectVersion=" + facts.get("objectVersion"));
        servletResponse.setStatus(statusCode);
        servletResponse.setContentType(facts.get("contentType"));
        servletResponse.setHeader("x-amz-request-id", facts.get("requestId"));
        servletResponse.setHeader("x-amz-version-id", facts.get("objectVersion"));
        servletResponse.setHeader("ETag", facts.get("ETag"));
        servletResponse.setHeader("x-amz-version-id", "1.0");
        if (facts.get("responseHeader:contentLength") != null) {
            servletResponse.setContentLengthLong(facts.get("responseHeader:contentLength"));
        }
        setIfAvailable(servletResponse, facts, "Last-Modified");
        if (facts.get("responseHeaders") != null) {
            Headers headers = facts.get("responseHeaders");
            headers.forEach(servletResponse::setHeader);
        }
        Object response = facts.get("response");
        if (response == null) {
            return;
        }
        if (response instanceof InputStream) {
            try (InputStream is = (InputStream) response) {
                ByteStreams.copy(is, servletResponse.getOutputStream());
            }
        } else {
            servletResponse.getWriter().write(response.toString());
        }
    }

    private void setIfAvailable(HttpServletResponse servletResponse, Facts facts, String headerName) {
        if (facts.get("responseHeader:" + headerName) != null) {
            servletResponse.setHeader(headerName, facts.get("responseHeader:" + headerName));
        }
    }

    private InputStream verifyContentMd5(InputStream sourceStream, String md5) {
        @SuppressWarnings("deprecation") HashingInputStream md5Stream = new HashingInputStream(Hashing.md5(), sourceStream);
        sourceStream = new InputStreamWrapper(md5Stream) {
            @Override
            public void close() throws IOException {
                super.close();
                String calculatedMd5 = md5Stream.hash().toString();
                logger.info("verifyContentMd5() md5=" + md5);
                logger.info("verifyContentMd5() calculatedMd5=" + calculatedMd5);
                if (!equalsIgnoreCase(md5, calculatedMd5)) {
                    throw new SpringStorageException(ErrorCode.BAD_DIGEST);
                }
            }
        };
        return sourceStream;
    }

    private InputStream verifyContentSha256(InputStream sourceStream, String sha256) {
        HashingInputStream sha256Stream = new HashingInputStream(Hashing.sha256(), sourceStream);
        sourceStream = new InputStreamWrapper(sha256Stream) {
            @Override
            public void close() throws IOException {
                super.close();
                String calculatedSha256 = sha256Stream.hash().toString();
                logger.info("verifyContentSha256() sha256=" + sha256);
                logger.info("verifyContentSha256() calculatedSha256=" + calculatedSha256);
                if (!equalsIgnoreCase(sha256, calculatedSha256)) {
                    throw new SpringStorageException(ErrorCode.X_AMZ_CONTENT_SHA256_MISMATCH);
                }
            }
        };
        return sourceStream;
    }

    private void writeError(HttpServletResponse response, Facts facts, ErrorCode errorCode) throws IOException {
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("x-amz-request-id", facts.get("requestId"));
        response.setHeader("x-amz-version-id", "1.0");
        response.setStatus(errorCode.getStatusCode());
        ErrorResponseXml error = new ErrorResponseXml();
        error.setRequestId(facts.get("requestId"));
        error.setHostId(facts.get("serverId"));
        error.setResource(facts.get("path"));
        error.setCode(errorCode.getName());
        error.setMessage(errorCode.getDescription());
        error.setBucketName(facts.get("bucketName"));
        error.setObjectName(facts.get("objectName"));
        response.getWriter().write(error.toString());
        logger.info("writeError() error=" + error);
    }

}
