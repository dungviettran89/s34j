package us.cuatoi.s34jserver.core;

import us.cuatoi.s34jserver.core.model.PutBucketRequest;
import us.cuatoi.s34jserver.core.model.S3Request;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

import static org.apache.commons.lang3.StringUtils.*;

public class S3RequestParser {

    private HttpServletRequest request;

    public S3RequestParser(HttpServletRequest request) {
        this.request = request;
    }

    public S3Request parse() throws IOException {
        S3Request s3Request = parseGenericInformation();
        return detectRequest(s3Request);
    }

    private S3Request detectRequest(S3Request s3Request) {
        String uri = s3Request.getUri();
        int slashCount = countMatches(uri, '/');
        boolean root = equalsIgnoreCase(uri, "/");
        String method = lowerCase(s3Request.getMethod());
        if (!root && slashCount == 1) {
            //bucket request
            String bucketName = substring(uri, 1);
            switch (method) {
                case "put":
                    return new PutBucketRequest(s3Request).setBucketName(bucketName);
            }
        }
        return s3Request;
    }

    private S3Request parseGenericInformation() throws IOException {
        S3Request s3Request = new S3Request()
                .setMethod(request.getMethod())
                .setUri(request.getRequestURI())
                .setUrl(request.getRequestURL().toString())
                .setQueryString(request.getQueryString())
                .setDate(request.getDateHeader("Date"));
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            s3Request.setHeader(lowerCase(header), request.getHeader(header));
        }
        Path content = Files.createTempFile(s3Request.getRequestId() + ".", ".tmp");
        Files.copy(request.getInputStream(), content, StandardCopyOption.REPLACE_EXISTING);
        s3Request.setContent(content);
        return s3Request;
    }
}
