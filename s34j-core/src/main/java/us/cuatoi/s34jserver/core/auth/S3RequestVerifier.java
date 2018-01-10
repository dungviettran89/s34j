package us.cuatoi.s34jserver.core.auth;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.model.bucket.BucketS3Request;
import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.operation.Verifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.ErrorCode.MISSING_SECURITY_HEADER;

public class S3RequestVerifier {
    private S3Context context;
    private S3Request s3Request;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private int maxDifferent = Integer.parseInt(System.getProperty("s34j.auth.request.maxDifferenceMinutes", "15")) * 60 * 1000;

    public S3RequestVerifier(S3Context context, S3Request s3Request) {
        this.context = context;
        this.s3Request = s3Request;
    }

    public void verify() {
        logger.debug("request=" + s3Request);
        logger.debug("contentFile=" + s3Request.getContent());
        verifyHeaders();
        verifyContent();
        if (s3Request instanceof BucketS3Request) {
            BucketS3Request bucketS3Request = (BucketS3Request) this.s3Request;
            Verifier.verifyBucketName(bucketS3Request.getBucketName());
        }
    }

    @SuppressWarnings("deprecation")
    private void verifyContent() {
        try {
            Path content = s3Request.getContent();
            long contentLength = Files.size(content);
            String providedSha256 = s3Request.getHeader("x-amz-content-sha256");
            String computedSha256 = contentLength > 0 ?
                    com.google.common.io.Files.asByteSource(content.toFile()).hash(Hashing.sha256()).toString() :
                    AWS4SignerBase.EMPTY_BODY_SHA256;
            if (!equalsIgnoreCase(computedSha256, providedSha256)) {
                throw new S3Exception(ErrorCode.BAD_DIGEST);
            }
            String providedMd5 = s3Request.getHeader("content-md5");
            if (contentLength > 0 && isNotBlank(providedMd5)) {
                String computedMd5 = com.google.common.io.Files.asByteSource(content.toFile()).hash(Hashing.md5()).toString();
                if (!equalsIgnoreCase(providedMd5, computedMd5)) {
                    throw new S3Exception(ErrorCode.INVALID_DIGEST);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new S3Exception(ErrorCode.INTERNAL_ERROR);
        }
    }

    private void verifyHeaders() {
        try {
            URL url = new URL(s3Request.getUrl());
            String authorizationHeader = s3Request.getHeader("authorization");
            AWS4Authorization authorization = new AWS4Authorization(authorizationHeader);
            String bodyHash = s3Request.getHeader("x-amz-content-sha256");
            if (isBlank(bodyHash)) {
                throw new S3Exception(MISSING_SECURITY_HEADER);
            }
            String method = s3Request.getMethod();
            String serviceName = authorization.getServiceName();
            String regionName = authorization.getRegionName();
            AWS4SignerForAuthorizationHeader signer = new AWS4SignerForAuthorizationHeader(url, method, serviceName, regionName);
            String awsAccessKey = authorization.getAwsAccessKey();
            String awsSecretKey = context.getSecretKey(awsAccessKey);

            if (isBlank(awsSecretKey)) {
                throw new S3Exception(ErrorCode.INVALID_ACCESS_KEY_ID);
            }
            HashMap<String, String> headers = new HashMap<>();
            for (String header : authorization.getSignedHeaders()) {
                if (!equalsAnyIgnoreCase(header, "host")) {
                    headers.put(header, s3Request.getHeader(header));
                }
            }
            String fullURL = s3Request.getUrl();
            if (isNotBlank(s3Request.getQueryString())) {
                fullURL += "?" + s3Request.getQueryString();
            }
            List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(new URI(fullURL), Charset.forName("UTF-8"));
            HashMap<String, String> queryParams = nameValuePairs.size() > 0 ? new HashMap<>() : null;
            for (NameValuePair nvp : nameValuePairs) {
                queryParams.put(nvp.getName(), nvp.getValue());
            }

            String amzDateHeader = s3Request.getHeader("x-amz-date");
            long dateHeader = s3Request.getDate();
            if (dateHeader <= 0 && isBlank(amzDateHeader)) {
                throw new S3Exception(MISSING_SECURITY_HEADER);
            }
            Date date = isBlank(amzDateHeader) ? new Date(dateHeader) :
                    AWS4Authorization.utcDateFormat(AWS4SignerBase.ISO8601BasicFormat).parse(amzDateHeader);
            if (Math.abs(date.getTime() - new Date().getTime()) > maxDifferent) {
                throw new S3Exception(ErrorCode.REQUEST_TIME_TOO_SKEWED);
            }
            String computedHeader = signer.computeSignature(headers, queryParams, bodyHash, awsAccessKey, awsSecretKey, date);
            logger.trace("fullURL=" + fullURL);
            logger.trace("headers=" + headers);
            logger.trace("parameters=" + queryParams);
            logger.trace("bodyHash=" + bodyHash);
            logger.trace("amzDateHeader=" + amzDateHeader);
            logger.trace("dateHeader=" + dateHeader);
            logger.trace("date=" + date);
            logger.trace("url=" + url);
            logger.trace("url.getHost()=" + url.getHost());
            logger.trace("url.getPort()=" + url.getPort());
            logger.trace("a=" + authorizationHeader);
            logger.trace("c=" + computedHeader);
            if (!StringUtils.equals(authorizationHeader, computedHeader)) {
                throw new S3Exception(ErrorCode.SIGNATURE_DOES_NOT_MATCH);
            }
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new S3Exception(ErrorCode.AUTHORIZATION_HEADER_MALFORMED);
        }
    }
}
