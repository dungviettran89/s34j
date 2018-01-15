package us.cuatoi.s34jserver.core.operation.object;

import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.dto.PostResponseDTO;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.model.object.PostObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.PostObjectS3Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_TEMPORARY_REDIRECT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PostObjectS3RequestHandler extends ObjectS3RequestHandler<PostObjectS3Request, PostObjectS3Response> {

    private final String policy;
    private final String awsAccessKeyId;
    private final String key;
    private final String successActionStatus;
    private final String successActionRedirect;

    public PostObjectS3RequestHandler(S3Context context, PostObjectS3Request s3Request) {
        super(context, s3Request);
        policy = s3Request.getFormParameter("policy");
        awsAccessKeyId = s3Request.getFormParameter("AWSAccessKeyId");
        key = s3Request.getFormParameter("key");
        successActionRedirect = s3Request.getFormParameter("success_action_redirect");
        successActionStatus = s3Request.getFormParameter("success_action_status");
    }

    @Override
    protected PostObjectS3Response handleObject() throws IOException {
        logger.debug("policy="+policy);
        logger.debug("awsAccessKeyId="+awsAccessKeyId);
        logger.debug("key="+key);
        logger.debug("successActionRedirect="+successActionRedirect);
        logger.debug("successActionStatus="+successActionStatus);

        //TODO: Handle policy validation
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

        PostResponseDTO dto = new PostResponseDTO();
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

    private int parseActionStatus() {
        try {
            return Integer.parseInt(successActionStatus);
        } catch (Exception ex) {
            return 204;
        }
    }
}
