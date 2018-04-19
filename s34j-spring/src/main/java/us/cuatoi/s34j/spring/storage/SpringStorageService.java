package us.cuatoi.s34j.spring.storage;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.apache.commons.lang3.StringUtils;
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
import us.cuatoi.s34j.spring.helper.InputStreamWrapper;
import us.cuatoi.s34j.spring.helper.SplitOutputStream;
import us.cuatoi.s34j.spring.operation.ExecutionRule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34j.spring.SpringStorageConstants.STREAMING_PAYLOAD;
import static us.cuatoi.s34j.spring.SpringStorageConstants.UNSIGNED_PAYLOAD;

@Service
public class SpringStorageService {
    private static final Logger logger = LoggerFactory.getLogger(SpringStorageService.class);
    @Value("${s34j.spring.blockSizeBytes:10485760}")//10MB
    private int blockSizeBytes;
    @Autowired
    private List<AuthenticationRule> authenticationRules;
    @Autowired
    private List<ExecutionRule> executionRules;


    public void handle(HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(response);

        String requestId = UUID.randomUUID().toString();
        String serverId = "springStorageService";
        //1: gather information
        Facts facts = new Facts();
        facts.put("response", response);
        facts.put("requestId", requestId);
        facts.put("serverId", serverId);
        facts.put("method", request.getMethod().toLowerCase());
        String path = StringUtils.trimToEmpty(request.getPathInfo());
        facts.put("path", path);
        String bucketName = path.substring(1);
        if (bucketName.length() > 0) {
            int firstSlashIndex = bucketName.indexOf('/');
            if (firstSlashIndex > 0) {
                bucketName = bucketName.substring(0, firstSlashIndex);
                facts.put("objectName", bucketName.substring(firstSlashIndex + 1));
            }
            facts.put("bucketName", bucketName);
        }
        Collections.list(request.getHeaderNames())
                .forEach((h) -> {
                    logger.debug("Header: " + h + "=" + request.getHeader(h));
                    facts.put("header:" + h, request.getHeader(h));
                });
        Collections.list(request.getParameterNames())
                .forEach((p) -> {
                    logger.debug("Form: " + p + "=" + request.getParameter(p));
                    facts.put("form:" + p, request.getParameter(p));
                });
        String url = request.getRequestURL().toString();
        facts.put("url", url);
        URLEncodedUtils.parse(new URI(url), UTF_8)
                .forEach((pair) -> {
                    logger.debug("Query: " + pair.getName() + "=" + pair.getValue());
                    facts.remove("form" + pair.getName());
                    facts.put("query:" + pair.getName(), pair.getValue());
                });

        try {
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
            InputStream sourceStream = request.getInputStream();
            //3.1: parse streaming payload
            //3.2: verify sha256
            String sha256 = facts.get("header:x-amz-content-sha256");
            boolean sha256Enabled = isNotBlank(sha256) && !equalsAny(sha256, STREAMING_PAYLOAD, UNSIGNED_PAYLOAD);
            if (sha256Enabled) {
                logger.info("handle() sha256Enabled=" + sha256Enabled);
                HashingInputStream sha256Stream = new HashingInputStream(Hashing.sha256(), sourceStream);
                sourceStream = new InputStreamWrapper(sha256Stream) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        String calculatedSha256 = sha256Stream.hash().toString();
                        logger.info("handle() sha256=" + sha256);
                        logger.info("handle() calculatedSha256=" + calculatedSha256);
                        if (!equalsIgnoreCase(sha256, calculatedSha256)) {
                            throw new SpringStorageException(ErrorCode.X_AMZ_CONTENT_SHA256_MISMATCH);
                        }
                    }
                };
            }
            //3.3 verify md5
            final String md5 = facts.get("header:content-md5");
            boolean md5Enabled = isNotBlank(md5);
            logger.info("handle() md5Enabled=" + md5Enabled);
            if (md5Enabled) {
                @SuppressWarnings("deprecation") HashingInputStream md5Stream = new HashingInputStream(Hashing.md5(), sourceStream);
                sourceStream = new InputStreamWrapper(md5Stream) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        String calculatedMd5 = md5Stream.hash().toString();
                        logger.info("handle() md5=" + md5);
                        logger.info("handle() calculatedMd5=" + calculatedMd5);
                        if (!equalsIgnoreCase(md5, calculatedMd5)) {
                            throw new SpringStorageException(ErrorCode.BAD_DIGEST);
                        }
                    }
                };
            }
            //3.5 parse stream
            try (InputStream is = sourceStream) {
                SplitOutputStream splitOutputStream = new SplitOutputStream(blockSizeBytes);
                try (SplitOutputStream os = splitOutputStream) {
                    long length = ByteStreams.copy(is, os);
                    facts.put("contentLength", length);
                }
                facts.put("parts", splitOutputStream.getInputStreams());
            }
            //4: perform execution
            Rules execution = new Rules();
            executionRules.forEach(execution::register);
            RulesEngine executionEngine = new DefaultRulesEngine();
            executionEngine.getParameters().setSkipOnFirstAppliedRule(true);
            executionEngine.fire(execution, facts);

            //4: response
            if (TRUE.equals(facts.get("responded"))) return;

            writeError(response, facts, ErrorCode.NOT_IMPLEMENTED);
        } catch (SpringStorageException storageException) {
            writeError(response, facts, storageException.getErrorCode());
        } finally {
            //clean up
            List<InputStream> partsToCleanUp = facts.get("parts");
            if (partsToCleanUp != null) {
                logger.info("handle() partsToCleanUp.size=" + partsToCleanUp.size());
                partsToCleanUp.forEach(Closeables::closeQuietly);
            }
        }
    }

    private void writeError(HttpServletResponse response, Facts facts, ErrorCode errorCode) throws IOException {
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("x-amz-request-id", facts.get("requestId"));
        response.setHeader("x-amz-version-id", "1.0");
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
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
