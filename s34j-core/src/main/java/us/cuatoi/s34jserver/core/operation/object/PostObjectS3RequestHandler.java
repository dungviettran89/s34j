package us.cuatoi.s34jserver.core.operation.object;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.dto.PostResponseXml;
import us.cuatoi.s34jserver.core.model.object.PostObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.PostObjectS3Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_TEMPORARY_REDIRECT;
import static org.apache.commons.lang3.StringUtils.*;

public class PostObjectS3RequestHandler extends ObjectS3RequestHandler<PostObjectS3Request, PostObjectS3Response> {

    private final String policy;
    private final String awsAccessKeyId;
    private final String successActionStatus;
    private final String successActionRedirect;

    public PostObjectS3RequestHandler(S3Context context, PostObjectS3Request s3Request) {
        super(context, s3Request);
        policy = s3Request.getFormParameter("policy");
        awsAccessKeyId = s3Request.getFormParameter("AWSAccessKeyId");
        successActionRedirect = s3Request.getFormParameter("success_action_redirect");
        successActionStatus = s3Request.getFormParameter("success_action_status");
    }

    @Override
    protected PostObjectS3Response handleObject() throws IOException {
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
            return (PostObjectS3Response) new PostObjectS3Response(s3Request)
                    .setHeader("success_action_redirect", successActionRedirect)
                    .setHeader("Location", redirectUrl)
                    .setHeader("ETag", eTag)
                    .setStatusCode(SC_TEMPORARY_REDIRECT);
        }

        PostResponseXml dto = new PostResponseXml();
        dto.setBucket(bucketName);
        dto.setKey(objectName);
        dto.setLocation(s3Request.getUrl());
        dto.seteTag(eTag);
        int statusCode = parseActionStatus();
        return (PostObjectS3Response) new PostObjectS3Response(s3Request)
                .setStatusCode(statusCode)
                .setHeader("ETag", eTag)
                .setContent(statusCode == 201 ? dto : null);
    }

    private void verifyPolicy() throws IOException {
        String policyJson = new String(BaseEncoding.base64().decode(this.policy), UTF_8);
        logger.trace("policyJson=" + policyJson);
        Policy policy = Policy.parse(policyJson);
        long expire = S3Constants.EXPIRATION_DATE_FORMAT.parseDateTime(policy.getExpiration()).getMillis();
        if (expire < System.currentTimeMillis()) {
            throw new S3Exception(ErrorCode.EXPIRED_TOKEN);
        }
        for (Policy.Condition condition : policy.getConditions()) {
            if (condition == null) {
                continue;
            } else if (equalsAnyIgnoreCase(condition.getOperator(), "eq", "start-with")) {
                switch (condition.getField()) {
                    case "$acl":
                        verify(condition, s3Request.getFormParameter("acl"));
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
                        verify(condition.setOperator("eq"), s3Request.getFormParameter("x-amz-credential"));
                        break;
                    case "$x-amz-date":
                        verify(condition.setOperator("eq"), s3Request.getFormParameter("x-amz-date"));
                        break;
                    default:
                        if (equalsAnyIgnoreCase(condition.getField(), STORED_HEADERS)) {
                            verify(condition, s3Request.getFormParameter(condition.getField()));
                        } else if (startsWith(condition.getField(), "x-amz-meta-")) {
                            verify(condition, s3Request.getFormParameter(condition.getField()));
                        } else if (startsWith(condition.getField(), "x-amz-")) {
                            verify(condition.setOperator("eq"), s3Request.getFormParameter(condition.getField()));
                        }
                        break;
                }
                logger.trace("Verified " + condition.getField() + " "
                        + condition.getOperator() + " " + condition.getValue());
            } else if (equalsAnyIgnoreCase(condition.getOperator(), "content-length-range")) {
                long size = Files.size(s3Request.getContent());
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

    private void verify(Policy.Condition condition, String valueToCheck) {
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

}
