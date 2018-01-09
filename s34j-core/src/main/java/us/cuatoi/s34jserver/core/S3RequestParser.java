package us.cuatoi.s34jserver.core;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import us.cuatoi.s34jserver.core.model.DeleteBucketS3Request;
import us.cuatoi.s34jserver.core.model.GetLocationBucketS3Request;
import us.cuatoi.s34jserver.core.model.PutBucketS3Request;
import us.cuatoi.s34jserver.core.model.S3Request;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.*;

public class S3RequestParser {

    private HttpServletRequest request;

    public S3RequestParser(HttpServletRequest request) {
        this.request = request;
    }

    public S3Request parse() throws IOException, URISyntaxException {
        S3Request s3Request = parseGenericInformation();
        return detectRequest(s3Request);
    }

    private S3Request detectRequest(S3Request s3Request) throws URISyntaxException {
        String uri = s3Request.getUri();
        int slashCount = countMatches(uri, '/');
        boolean root = equalsIgnoreCase(uri, "/");
        String method = lowerCase(s3Request.getMethod());
        String fullURL = s3Request.getUrl();
        if (isNotBlank(s3Request.getQueryString())) {
            fullURL += "?" + s3Request.getQueryString();
        }
        Map<String, String> queryParams = new HashMap<>();
        for (NameValuePair pair : URLEncodedUtils.parse(new URI(fullURL), Charset.forName("UTF-8"))) {
            queryParams.put(pair.getName(), pair.getValue());
        }
        if (!root && slashCount == 1) {
            //bucket request
            String bucketName = substring(uri, 1);
            if (equalsIgnoreCase(method, "put")) {
                return new PutBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && queryParams.get("location") != null) {
                return new GetLocationBucketS3Request(s3Request).setBucketName(bucketName);
            } else if(equalsIgnoreCase(method,"delete")){
                return new DeleteBucketS3Request(s3Request).setBucketName(bucketName);
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
