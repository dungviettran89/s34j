package us.cuatoi.s34j.spring.storage;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.auth.AuthenticationRule;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.dto.ErrorResponseXml;
import us.cuatoi.s34j.spring.operation.ExecutionRule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class SpringStorageService {
    private static final Logger logger = LoggerFactory.getLogger(SpringStorageService.class);
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
        facts.put("request", request);
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
        URLEncodedUtils.parse(new URI(request.getRequestURL().toString()), UTF_8)
                .forEach((pair) -> {
                    logger.debug("Query: " + pair.getName() + "=" + pair.getValue());
                    facts.remove("form" + pair.getName());
                    facts.put("query:" + pair.getName(), pair.getValue());
                });


        //2: perform authentication
        Rules authentication = new Rules();
        authenticationRules.forEach(authentication::register);
        RulesEngine authenticationEngine = new DefaultRulesEngine();
        authenticationEngine.fire(authentication, facts);
        if (!TRUE.equals(facts.get("authenticated"))) {
            ErrorCode errorCode = facts.get("errorCode");
            if (errorCode == null) {
                errorCode = ErrorCode.ACCESS_DENIED;
            }
            writeError(response, facts, errorCode);
            return;
        }

        //3: perform execution
        Rules execution = new Rules();
        executionRules.forEach(execution::register);
        RulesEngine executionEngine = new DefaultRulesEngine();
        executionEngine.getParameters().setSkipOnFirstAppliedRule(true);
        executionEngine.fire(execution, facts);

        //4: response
        if (TRUE.equals(facts.get("responded"))) return;

        writeError(response, facts, ErrorCode.NOT_IMPLEMENTED);
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
