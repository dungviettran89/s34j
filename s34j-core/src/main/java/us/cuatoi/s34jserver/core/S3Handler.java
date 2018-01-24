package us.cuatoi.s34jserver.core;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.dto.ErrorResponseXml;
import us.cuatoi.s34jserver.core.model.GetBucketsS3Request;
import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.S3Response;
import us.cuatoi.s34jserver.core.model.bucket.*;
import us.cuatoi.s34jserver.core.model.object.*;
import us.cuatoi.s34jserver.core.model.object.multipart.*;
import us.cuatoi.s34jserver.core.operation.GetBucketsS3RequestHandler;
import us.cuatoi.s34jserver.core.operation.S3RequestHandler;
import us.cuatoi.s34jserver.core.operation.bucket.*;
import us.cuatoi.s34jserver.core.operation.object.*;
import us.cuatoi.s34jserver.core.operation.object.multipart.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static us.cuatoi.s34jserver.core.helper.LogHelper.traceMultiline;

public class S3Handler {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private S3Context context;

    public S3Handler(S3Context s3Context, HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        context = s3Context;
    }


    public void handle() throws IOException {
        //Start:
        printDebugInfo();
        S3Request s3Request = new S3Request()
                .setRequestId(UUID.randomUUID().toString())
                .setServerId(context.getServerId());
        try {
            //Step 1: parse request
            s3Request = new S3RequestParserVerifier(context, request, s3Request).execute();
            //Step 3: execute request
            S3RequestHandler handler = getHandler(s3Request);
            if (handler != null) {
                returnResponse(handler.handle());
            } else {
                returnError(s3Request, new S3Exception(ErrorCode.NOT_IMPLEMENTED));
            }
        } catch (S3Exception ex) {
            returnError(s3Request, ex);
        } catch (Exception ex) {
            logger.error("Unexpected error.", ex);
            returnError(s3Request, new S3Exception(ErrorCode.INTERNAL_ERROR));
        } finally {
            //Final: clean up
            if (s3Request != null && s3Request.getContent() != null) {
                Files.delete(s3Request.getContent());
            }
        }
    }

    private S3RequestHandler getHandler(S3Request s3Request) {
        //TODO: Refactor this
        if (s3Request instanceof GetBucketsS3Request) {
            return new GetBucketsS3RequestHandler(context, (GetBucketsS3Request) s3Request);
        } else if (s3Request instanceof PutBucketS3Request) {
            return new PutBucketS3RequestHandler(context, (PutBucketS3Request) s3Request);
        } else if (s3Request instanceof GetBucketLocationBucketS3Request) {
            return new GetBucketLocationBucketS3RequestHandler(context, (GetBucketLocationBucketS3Request) s3Request);
        } else if (s3Request instanceof ListBucketMultipartUploadsS3Request) {
            return new ListBucketMultipartUploadsS3RequestHandler(context, (ListBucketMultipartUploadsS3Request) s3Request);
        } else if (s3Request instanceof DeleteBucketS3Request) {
            return new DeleteBucketS3RequestHandler(context, (DeleteBucketS3Request) s3Request);
        } else if (s3Request instanceof HeadBucketS3Request) {
            return new HeadBucketS3RequestHandler(context, (HeadBucketS3Request) s3Request);
        } else if (s3Request instanceof PutObjectS3Request) {
            return new PutObjectS3RequestHandler(context, (PutObjectS3Request) s3Request);
        } else if (s3Request instanceof DeleteObjectS3Request) {
            return new DeleteObjectS3RequestHandler(context, (DeleteObjectS3Request) s3Request);
        } else if (s3Request instanceof InitiateMultipartUploadObjectS3Request) {
            return new InitiateMultipartUploadObjectS3RequestHandler(context, (InitiateMultipartUploadObjectS3Request) s3Request);
        } else if (s3Request instanceof UploadPartObjectS3Request) {
            return new UploadPartObjectS3RequestHandler(context, (UploadPartObjectS3Request) s3Request);
        } else if (s3Request instanceof CompleteMultipartUploadObjectS3Request) {
            return new CompleteMultipartUploadObjectS3RequestHandler(context, (CompleteMultipartUploadObjectS3Request) s3Request);
        } else if (s3Request instanceof HeadObjectS3Request) {
            return new HeadObjectS3RequestHandler(context, (HeadObjectS3Request) s3Request);
        } else if (s3Request instanceof GetObjectS3Request) {
            return new GetObjectS3RequestHandler(context, (GetObjectS3Request) s3Request);
        } else if (s3Request instanceof AbortMultipartUploadObjectS3Request) {
            return new AbortMultipartUploadObjectS3RequestHandler(context, (AbortMultipartUploadObjectS3Request) s3Request);
        } else if (s3Request instanceof ListObjectsV1S3Request) {
            return new ListObjectsV1S3RequestHandler(context, (ListObjectsV1S3Request) s3Request);
        } else if (s3Request instanceof ListObjectsV2S3Request) {
            return new ListObjectsV2S3RequestHandler(context, (ListObjectsV2S3Request) s3Request);
        } else if (s3Request instanceof DeleteMultipleObjectsS3Request) {
            return new DeleteMultipleObjectsS3RequestHandler(context, (DeleteMultipleObjectsS3Request) s3Request);
        } else if (s3Request instanceof ListMultipartUploadPartsS3Request) {
            return new ListMultipartUploadPartsS3RequestHandler(context, (ListMultipartUploadPartsS3Request) s3Request);
        } else if (s3Request instanceof PostObjectS3Request) {
            return new PostObjectS3RequestHandler(context, (PostObjectS3Request) s3Request);
        }else if (s3Request instanceof HandlePolicyBucketS3Request) {
            return new HandlePolicyBucketS3RequestHandler(context, (HandlePolicyBucketS3Request) s3Request);
        }
        return null;
    }

    private void returnResponse(S3Response s3Response) throws IOException {
        traceMultiline(logger, "Response=" + s3Response);

        response.setStatus(s3Response.getStatusCode());
        s3Response.getHeaders().forEach((k, v) -> {
            response.setHeader(k, v);
        });
        if (s3Response.getContent() instanceof Path) {
            response.setContentType(s3Response.getContentType());
            Path contentFile = (Path) s3Response.getContent();
            response.setContentLengthLong(Files.size(contentFile));
            try (InputStream is = Files.newInputStream(contentFile)) {
                IOUtils.copy(is, response.getOutputStream());
            }
        } else if (s3Response.getContent() != null) {
            response.setContentType(S3Constants.CONTENT_TYPE);
            response.getWriter().write(s3Response.getContent().toString());
            logger.debug("Content=" + s3Response.getContent().toString());
        }
        logger.debug("-------- END " + debugInfo() + " ----------------------");
    }

    private String debugInfo() {
        return request.getMethod() + " " + request.getRequestURL() + (isBlank(request.getQueryString()) ? "" : "?" + request.getQueryString());
    }

    private void returnError(S3Request s3Request, S3Exception exception) throws IOException {
        response.setStatus(exception.getStatusCode());
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("x-amz-request-id", s3Request.getRequestId());
        response.setHeader("x-amz-version-id", "1.0");

        ErrorResponseXml errorResponse = new ErrorResponseXml();
        errorResponse.setRequestId(s3Request.getRequestId());
        errorResponse.setHostId(s3Request.getServerId());
        errorResponse.setResource(s3Request.getUri());
        errorResponse.setCode(exception.getName());
        errorResponse.setMessage(exception.getDescription());
        if (s3Request instanceof BucketS3Request) {
            errorResponse.setBucketName(((BucketS3Request) s3Request).getBucketName());
        }
        response.getWriter().write(errorResponse.toString());
        logger.debug("Error=" + errorResponse);
    }

    private void printDebugInfo() {
        logger.debug("-------- START " + debugInfo() + " ----------------------");
        logger.trace("request.getMethod=" + request.getMethod());
        logger.trace("request.getPathInfo=" + request.getPathInfo());
        logger.trace("request.getRequestURI=" + request.getRequestURI());
        logger.trace("request.getRequestURL=" + request.getRequestURL());
        logger.trace("request.getServletPath=" + request.getServletPath());
        Enumeration<String> headerNames = request.getHeaderNames();
        logger.trace("request.getHeaders()=");
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            logger.trace("\t" + header + "=" + request.getHeader(header));
        }
    }
}
