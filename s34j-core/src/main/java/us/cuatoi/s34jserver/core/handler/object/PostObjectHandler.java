package us.cuatoi.s34jserver.core.handler.object;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.dto.PostResponseXml;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.handler.bucket.BucketHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_TEMPORARY_REDIRECT;
import static org.apache.commons.lang3.StringUtils.*;

public class PostObjectHandler extends ObjectHandler {

    private final String policy;
    private final String awsAccessKeyId;
    private final String successActionStatus;
    private final String successActionRedirect;

    protected PostObjectHandler(StorageContext context, Request request) {
        super(context, request);
        policy = request.getFormParameter("policy");
        awsAccessKeyId = request.getFormParameter("AWSAccessKeyId");
        successActionRedirect = request.getFormParameter("success_action_redirect");
        successActionStatus = request.getFormParameter("success_action_status");
    }

    @Override
    protected String getName() {
        return "s3:PutObject";
    }

    @Override
    public Response handle() throws Exception {
        logger.debug("policy=" + policy);
        logger.debug("awsAccessKeyId=" + awsAccessKeyId);
        logger.debug("successActionRedirect=" + successActionRedirect);
        logger.debug("successActionStatus=" + successActionStatus);

        verifyPolicy();

        String eTag = saveObjectAndMetadata();

        String redirectUrl;
        try {
            redirectUrl = new URL(successActionRedirect).toString();
        } catch (MalformedURLException url) {
            logger.info("Invalid success_action_redirect " + successActionRedirect);
            redirectUrl = null;
        }

        if (isNotBlank(redirectUrl)) {
            return new Response()
                    .setHeader("success_action_redirect", successActionRedirect)
                    .setHeader("Location", redirectUrl)
                    .setHeader("ETag", eTag)
                    .setStatus(SC_TEMPORARY_REDIRECT);
        }

        PostResponseXml dto = new PostResponseXml();
        dto.setBucket(bucketName);
        dto.setKey(objectName);
        dto.setLocation(request.getUrl());
        dto.seteTag(eTag);
        int statusCode = parseActionStatus();
        return new Response().setStatus(statusCode).setHeader("ETag", eTag)
                .setContent(statusCode == 201 ? dto : null);
    }

    private void verifyPolicy() throws IOException {
        String policyJson = new String(BaseEncoding.base64().decode(this.policy), UTF_8);
        logger.trace("policyJson=" + policyJson);
        PostPolicy postPolicy = PostPolicy.parse(policyJson);
        long expire = S3Constants.EXPIRATION_DATE_FORMAT.parseDateTime(postPolicy.getExpiration()).getMillis();
        if (expire < System.currentTimeMillis()) {
            throw new S3Exception(ErrorCode.EXPIRED_TOKEN);
        }
        for (PostPolicy.Condition condition : postPolicy.getConditions()) {
            if (condition == null) {
                continue;
            } else if (equalsAnyIgnoreCase(condition.getOperator(), "eq", "start-with")) {
                switch (condition.getField()) {
                    case "$acl":
                        verify(condition, request.getFormParameter("acl"));
                        break;
                    case "$key":
                        verify(condition, objectName);
                        break;
                    case "$bucket":
                        verify(condition, bucketName);
                        break;
                    case "$success_action_redirect":
                        verify(condition, successActionRedirect);
                        break;
                    case "$success_action_status":
                        verify(condition.setOperator("eq"), successActionStatus);
                        break;
                    case "$x-amz-algorithm":
                        verify(condition.setOperator("eq"), "AWS4-HMAC-SHA256");
                        break;
                    case "$x-amz-credential":
                        verify(condition.setOperator("eq"), request.getFormParameter("x-amz-credential"));
                        break;
                    case "$x-amz-date":
                        verify(condition.setOperator("eq"), request.getFormParameter("x-amz-date"));
                        break;
                    default:
                        if (equalsAnyIgnoreCase(condition.getField(), STORED_HEADERS)) {
                            verify(condition, request.getFormParameter(condition.getField()));
                        } else if (startsWith(condition.getField(), "x-amz-meta-")) {
                            verify(condition, request.getFormParameter(condition.getField()));
                        } else if (startsWith(condition.getField(), "x-amz-")) {
                            verify(condition.setOperator("eq"), request.getFormParameter(condition.getField()));
                        }
                        break;
                }
                logger.trace("Verified " + condition.getField() + " "
                        + condition.getOperator() + " " + condition.getValue());
            } else if (equalsAnyIgnoreCase(condition.getOperator(), "content-length-range")) {
                long size = Files.size(request.getContent());
                long upperBound = condition.getUpperBound();
                if (size > upperBound) {
                    logger.info("ENTITY_TOO_LARGE size=" + size);
                    logger.info("ENTITY_TOO_LARGE upperBound=" + upperBound);
                    throw new S3Exception(ErrorCode.ENTITY_TOO_LARGE);
                }
                long lowerBound = condition.getLowerBound();
                if (size < lowerBound) {
                    logger.info("ENTITY_TOO_SMALL size=" + size);
                    logger.info("ENTITY_TOO_SMALL lowerBound=" + lowerBound);
                    throw new S3Exception(ErrorCode.ENTITY_TOO_SMALL);
                }
                logger.trace(String.format("Verified %s in range [%s,%s]", condition.getField(), lowerBound, upperBound));
            }

        }
    }

    private void verify(PostPolicy.Condition condition, String valueToCheck) {
        if (equalsIgnoreCase(condition.getOperator(), "eq") && !StringUtils.equals(condition.getValue(), valueToCheck)) {
            logger.info("ACCESS_DENIED " + condition.getValue() + "!=" + valueToCheck);
            throw new S3Exception(ErrorCode.ACCESS_DENIED);
        } else if (!startsWith(valueToCheck, condition.getValue())) {
            logger.info("ACCESS_DENIED " + valueToCheck + " does not start with " + condition.getValue());
            throw new S3Exception(ErrorCode.ACCESS_DENIED);
        }
    }

    private int parseActionStatus() {
        try {
            return Integer.parseInt(successActionStatus);
        } catch (Exception ex) {
            return 204;
        }
    }

    public static class Builder extends BucketHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isNotBlank(request.getObjectName());
            ok = ok && equalsIgnoreCase(request.getMethod(), "post");
                    ok = ok && !containsAny(request.getQueryString(), "uploads", "uploadId");
            ok = ok && request.getFormParameter("fileName") == null;
            return ok;
        }

        @Override
        public BaseHandler create(StorageContext context, Request request) {
            return new PostObjectHandler(context, request);
        }
    }
}
