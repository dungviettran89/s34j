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
import us.cuatoi.s34jserver.core.model.object.HeadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.PutObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.AbortMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.CompleteMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.InitiateMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.UploadPartObjectS3Request;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

import static org.apache.commons.lang3.StringUtils.*;

public class S3RequestParser {

    private S3Request s3Request;
    private HttpServletRequest request;

    public S3RequestParser(HttpServletRequest request, S3Request s3Request) {
        this.request = request;
        this.s3Request = s3Request;
    }

    public S3Request fillAndDetect() throws Exception {
        fillGenericInformation();
        return detectRequest();
    }

    private S3Request detectRequest() throws Exception {
        String uri = s3Request.getUri();
        int slashCount = countMatches(uri, '/');
        boolean root = equalsIgnoreCase(uri, "/");
        String method = lowerCase(s3Request.getMethod());
        boolean noQueryParams = s3Request.getQueryParameters().size() == 0;
        if (root && noQueryParams) {
            //root request
            return new GetBucketsS3Request(s3Request);
        } else if (slashCount == 1) {
            //bucket request
            String bucketName = substring(uri, 1);
            if (equalsIgnoreCase(method, "put") && noQueryParams) {
                return new PutBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && s3Request.getQueryParameter("location") != null) {
                return new GetLocationBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && s3Request.getQueryParameter("uploads") != null) {
                return new GetLocationBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete") && noQueryParams) {
                return new ListMultipartUploadsBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "head") && noQueryParams) {
                return new HeadBucketS3Request(s3Request).setBucketName(bucketName);
            }
        } else {
            int secondSlash = indexOf(uri, '/', 2);
            String bucketName = substring(uri, 1, secondSlash);
            String objectName = substring(uri, secondSlash + 1);
            if (equalsIgnoreCase(method, "put") && noQueryParams) {
                return new PutObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete") && noQueryParams) {
                return new DeleteObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && noQueryParams) {
                return new GetObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "head") && noQueryParams) {
                return new HeadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "post") && s3Request.getQueryParameter("uploads") != null) {
                return new InitiateMultipartUploadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "put") && s3Request.getQueryParameter("uploadId") != null) {
                return new UploadPartObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete") && s3Request.getQueryParameter("uploadId") != null) {
                return new AbortMultipartUploadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
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
        try (BufferedReader br = Files.newBufferedReader(s3Request.getContent(), StandardCharsets.UTF_8)) {
            XmlPullParser parser = Xml.createParser();
            parser.setInput(br);
            Xml.parseElement(parser, dto, dto.getNamespaceDictionary(), null);
        }
        return dto;
    }

    private S3Request fillGenericInformation() throws IOException, URISyntaxException {
        s3Request.setMethod(request.getMethod())
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
        for (NameValuePair pair : URLEncodedUtils.parse(new URI(fullURL), StandardCharsets.UTF_8)) {
            s3Request.setQueryParameter(pair.getName(), pair.getValue());
        }
        if (!equalsIgnoreCase(request.getMethod(), "get")) {
            String contentEncoding = s3Request.getHeader("content-encoding");
            if (equalsIgnoreCase("aws-chunked", contentEncoding)) {
                throw new S3Exception(ErrorCode.NOT_IMPLEMENTED);
            } else {
                Path content = Files.createTempFile(s3Request.getRequestId() + ".", ".tmp");
                Files.copy(request.getInputStream(), content, StandardCopyOption.REPLACE_EXISTING);
                s3Request.setContent(content);
            }
        }
        return s3Request;
    }
}
