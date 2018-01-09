package us.cuatoi.s34jserver.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.auth.S3RequestVerifier;
import us.cuatoi.s34jserver.core.model.S3Request;

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
            new S3RequestVerifier(context,s3Request).verify();
            //Step 3: execute request
            //Step 4: response
        } finally {
            //Final: clean up
            if (s3Request != null && s3Request.getContent() != null) {
                Files.delete(s3Request.getContent());
            }
        }
    }

    private void verifyRequest(S3Request s3Request) {

    }


    private void printDebugInfo() {
        logger.trace("--------" + request.getMethod() + " " + request.getRequestURL() + " ----------------------");
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
