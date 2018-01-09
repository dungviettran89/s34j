package us.cuatoi.s34jserver.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.auth.S3RequestVerifier;
import us.cuatoi.s34jserver.core.dto.ErrorResponse;
import us.cuatoi.s34jserver.core.model.BucketS3Request;
import us.cuatoi.s34jserver.core.model.PutBucketS3Request;
import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.S3Response;
import us.cuatoi.s34jserver.core.operation.bucket.PutBucketS3RequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;

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
        S3Request s3Request = null;
        try {
            //Step 1: parse request
            s3Request = new S3RequestParser(request).parse();
            s3Request = s3Request.setServerId(context.getServerId());
            //Step 2: verify request
            new S3RequestVerifier(context, s3Request).verify();
            //Step 3: execute request
            if (s3Request instanceof PutBucketS3Request) {
                S3Response response = new PutBucketS3RequestHandler(context, (PutBucketS3Request) s3Request).handle();
                returnResponse(response);
            } else {
                returnError(s3Request, new S3Exception(ErrorCode.NOT_IMPLEMENTED));
            }
        } catch (S3Exception ex) {
            returnError(s3Request, ex);
        } catch (Exception ex) {
            returnError(s3Request, new S3Exception(ErrorCode.INTERNAL_ERROR));
        } finally {
            //Final: clean up
            if (s3Request != null && s3Request.getContent() != null) {
                Files.delete(s3Request.getContent());
            }
        }
    }

    private void returnResponse(S3Response s3Response) {
        response.setStatus(s3Response.getStatusCode());
        s3Response.getHeaders().forEach((k, v) -> {
            response.setHeader(k, v);
        });
        logger.info("Response=" + s3Response);
        logger.debug("-------- END " + request.getMethod() + " " + request.getRequestURL() + " ----------------------");
    }

    private void returnError(S3Request s3Request, S3Exception exception) throws IOException {
        response.setStatus(exception.getStatusCode());
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("x-amz-request-id", s3Request.getRequestId());
        response.setHeader("x-amz-version-id", "1.0");

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setRequestId(s3Request.getRequestId());
        errorResponse.setHostId(s3Request.getServerId());
        errorResponse.setResource(s3Request.getUri());
        errorResponse.setCode(exception.getName());
        errorResponse.setMessage(exception.getDescription());
        if (s3Request instanceof BucketS3Request) {
            errorResponse.setBucketName(((BucketS3Request) s3Request).getBucketName());
        }
        response.getWriter().write(errorResponse.toString());
    }

    private void printDebugInfo() {
        logger.debug("-------- START " + request.getMethod() + " " + request.getRequestURL() + " ----------------------");
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
