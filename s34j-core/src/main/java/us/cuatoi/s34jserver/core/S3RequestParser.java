package us.cuatoi.s34jserver.core;

import com.google.api.client.xml.Xml;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import us.cuatoi.s34jserver.core.dto.CompleteMultipartUploadDTO;
import us.cuatoi.s34jserver.core.dto.GenericDTO;
import us.cuatoi.s34jserver.core.model.GetBucketsS3Request;
import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.bucket.GetLocationBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.HeadBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.ListMultipartUploadsBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.PutBucketS3Request;
import us.cuatoi.s34jserver.core.model.object.DeleteObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.GetObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.PutObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.CompleteMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.InitiateMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.UploadPartObjectS3Request;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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

    public S3Request parse() throws Exception {
        S3Request s3Request = parseGenericInformation();
        return detectRequest(s3Request);
    }

    private S3Request detectRequest(S3Request s3Request) throws Exception {
        String uri = s3Request.getUri();
        int slashCount = countMatches(uri, '/');
        boolean root = equalsIgnoreCase(uri, "/");
        String method = lowerCase(s3Request.getMethod());
        boolean noQueryParams = s3Request.getQueryParameters().size() == 0;
        if (root) {
            //root request
            return new GetBucketsS3Request(s3Request);
        } else if (slashCount == 1) {
            //bucket request
            String bucketName = substring(uri, 1);
            if (equalsIgnoreCase(method, "put")) {
                return new PutBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && s3Request.getQueryParameter("location") != null) {
                return new GetLocationBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && s3Request.getQueryParameter("uploads") != null) {
                return new GetLocationBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete")) {
                return new ListMultipartUploadsBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "head")) {
                return new HeadBucketS3Request(s3Request).setBucketName(bucketName);
            }
        } else {
            int secondSlash = indexOf(uri, '/', 2);
            String bucketName = substring(uri, 1, secondSlash);
            String objectName = substring(uri, secondSlash + 1);
            if (equalsIgnoreCase(method, "put") && noQueryParams) {
                return new PutObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete")) {
                return new DeleteObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get")) {
                return new GetObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "post") && s3Request.getQueryParameter("uploads") != null) {
                return new InitiateMultipartUploadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "put") && s3Request.getQueryParameter("uploadId") != null) {
                return new UploadPartObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "post") && s3Request.getQueryParameter("uploadId") != null) {
                CompleteMultipartUploadDTO dto = parseXmlContent(s3Request, new CompleteMultipartUploadDTO());
                return new CompleteMultipartUploadObjectS3Request(s3Request)
                        .setCompleteMultipartUploadDTO(dto)
                        .setObjectName(objectName).setBucketName(bucketName);
            }
        }
        return s3Request;
    }

    private <D extends GenericDTO> D parseXmlContent(S3Request s3Request, D dto) throws IOException, XmlPullParserException {
        try (BufferedReader br = Files.newBufferedReader(s3Request.getContent(), Charset.forName("UTF-8"))) {
            XmlPullParser parser = Xml.createParser();
            parser.setInput(br);
            Xml.parseElement(parser, dto, dto.getNamespaceDictionary(), null);
        }
        return dto;
    }

    private S3Request parseGenericInformation() throws IOException, URISyntaxException {
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
        String fullURL = s3Request.getUrl();
        if (isNotBlank(s3Request.getQueryString())) {
            fullURL += "?" + s3Request.getQueryString();
        }
        for (NameValuePair pair : URLEncodedUtils.parse(new URI(fullURL), Charset.forName("UTF-8"))) {
            s3Request.setQueryParameter(pair.getName(), pair.getValue());
        }
        Path content = Files.createTempFile(s3Request.getRequestId() + ".", ".tmp");
        Files.copy(request.getInputStream(), content, StandardCopyOption.REPLACE_EXISTING);
        s3Request.setContent(content);
        return s3Request;
    }
}
