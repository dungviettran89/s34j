package us.cuatoi.s34jserver.core.auth;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.S3Request;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.ErrorCode.AUTHORIZATION_HEADER_MALFORMED;
import static us.cuatoi.s34jserver.core.ErrorCode.MISSING_SECURITY_HEADER;
import static us.cuatoi.s34jserver.core.auth.AWS4SignerForChunkedUpload.STREAMING_BODY_SHA256;
import static us.cuatoi.s34jserver.core.helper.PathHelper.md5HashFileToByte;

public class S3RequestVerifier {
    private S3Context context;
    private S3Request s3Request;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private AWS4SignerForChunkedUpload aws4SignerForChunkedUpload;

    public S3RequestVerifier(S3Context context, S3Request s3Request) {
        this.context = context;
        this.s3Request = s3Request;
    }

    @SuppressWarnings("deprecation")
    public void verifySingleChunk() throws IOException {
        String providedSha256 = s3Request.getHeader("x-amz-content-sha256");
        Path content = s3Request.getContent();
        logger.trace("Checking " + content);
        long contentLength = Files.size(content);
        String computedSha256 = contentLength > 0 ? PathHelper.sha256HashFile(content) : AWS4SignerBase.EMPTY_BODY_SHA256;
        if (!equalsIgnoreCase(computedSha256, providedSha256)) {
            logger.info("INVALID_DIGEST: providedSha256="+providedSha256);
            logger.info("INVALID_DIGEST: computedSha256="+computedSha256);
            throw new S3Exception(ErrorCode.INVALID_DIGEST);
        }
        String providedMd5 = s3Request.getHeader("content-md5");
        if (contentLength > 0 && isNotBlank(providedMd5)) {
            String computedMd5 = BaseEncoding.base64().encode(md5HashFileToByte(content));
            if (!equalsIgnoreCase(providedMd5, computedMd5)) {
                logger.info("INVALID_DIGEST: providedMd5="+providedMd5);
                logger.info("INVALID_DIGEST: computedMd5="+computedMd5);
                throw new S3Exception(ErrorCode.INVALID_DIGEST);
            }
        }
    }

    public void verifyHeaders() {
        URL url = newURLUnchecked(s3Request.getUrl());

        String authorizationHeader = s3Request.getHeader("authorization");
        if(isBlank(authorizationHeader)){
            String algorithm = s3Request.getQueryParameter("X-Amz-Algorithm");
            String credential = s3Request.getQueryParameter("X-Amz-Credential");
            String date = s3Request.getQueryParameter("X-Amz-Date");
            String signedHeaders = s3Request.getQueryParameter("X-Amz-SignedHeaders");
            String signature = s3Request.getQueryParameter("X-Amz-Signature");
            if (isNoneBlank(algorithm, credential, date, signedHeaders, signature)) {
                authorizationHeader = algorithm +
                        " Credential=" + credential +
                        ", SignedHeaders=" + signedHeaders +
                        ", Signature=" + signature;
                logger.debug("Constructed authorizationHeader based on Query String:" + authorizationHeader);
            }
        }
        if (isBlank(authorizationHeader)) {
            logger.info("MISSING_SECURITY_HEADER authorizationHeader=" + authorizationHeader);
            throw new S3Exception(MISSING_SECURITY_HEADER);
        }

        String amzDateHeader = s3Request.getHeader("x-amz-date");
        if (isBlank(amzDateHeader)) {
            amzDateHeader = s3Request.getQueryParameter("X-Amz-Date");
        }

        AWS4Authorization authorization = new AWS4Authorization(authorizationHeader);
        String bodyHash = s3Request.getHeader("x-amz-content-sha256");
        if (isBlank(bodyHash)) {
            bodyHash = "UNSIGNED-PAYLOAD";
        }

        String method = s3Request.getMethod();
        String serviceName = authorization.getServiceName();
        String regionName = authorization.getRegionName();
        AWS4SignerForAuthorizationHeader signer = new AWS4SignerForAuthorizationHeader(url, method, serviceName, regionName);
        aws4SignerForChunkedUpload = new AWS4SignerForChunkedUpload(url, method, serviceName, regionName);

        String awsAccessKey = authorization.getAwsAccessKey();
        String awsSecretKey = context.getSecretKey(awsAccessKey);
        if (isBlank(awsSecretKey)) {
            logger.info("INVALID_ACCESS_KEY_ID awsAccessKey=" + awsAccessKey);
            logger.info("INVALID_ACCESS_KEY_ID awsSecretKey=" + awsSecretKey);
            throw new S3Exception(ErrorCode.INVALID_ACCESS_KEY_ID);
        }
        HashMap<String, String> headers = new HashMap<>();
        for (String header : authorization.getSignedHeaders()) {
            if (!equalsAnyIgnoreCase(header, "host")) {
                headers.put(header, s3Request.getHeader(header));
            }
        }

        String fullURL = s3Request.getFullUrl();
        List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(newURIUnchecked(fullURL), StandardCharsets.UTF_8);
        HashMap<String, String> queryParams = nameValuePairs.size() > 0 ? new HashMap<>() : null;
        for (NameValuePair nvp : nameValuePairs) {
            if(!equalsIgnoreCase(nvp.getName(),"X-Amz-Signature")) {
                queryParams.put(nvp.getName(), nvp.getValue());
            }
        }
        Date date = getRequestDate(amzDateHeader);
        Date now = new Date();
        String expiresParams = s3Request.getQueryParameter("X-Amz-Expires");
        if (isNotBlank(expiresParams)) {
            int expires = Integer.parseInt(expiresParams) * 1000;
            if (Math.abs(date.getTime() - now.getTime()) > expires) {
                logger.info("EXPIRED_TOKEN currentDate=" + now);
                logger.info("EXPIRED_TOKEN requestDate=" + date);
                throw new S3Exception(ErrorCode.EXPIRED_TOKEN);
            }
        } else {
            String expiresDefault = System.getProperty("s34j.auth.request.maxDifferenceMinutes", "15");
            int expires = Integer.parseInt(expiresDefault) * 60 * 1000;
            if (Math.abs(date.getTime() - now.getTime()) > expires) {
                logger.info("REQUEST_TIME_TOO_SKEWED currentDate=" + now);
                logger.info("REQUEST_TIME_TOO_SKEWED requestDate=" + date);
                throw new S3Exception(ErrorCode.REQUEST_TIME_TOO_SKEWED);
            }
        }

        String computedHeader = signer.computeSignature(headers, queryParams, bodyHash, awsAccessKey, awsSecretKey, date);
        aws4SignerForChunkedUpload.computeSignature(headers, queryParams, STREAMING_BODY_SHA256, awsAccessKey, awsSecretKey, date);
        logger.trace("fullURL=" + fullURL);
        logger.trace("headers=" + headers);
        logger.trace("parameters=" + queryParams);
        logger.trace("bodyHash=" + bodyHash);
        logger.trace("amzDateHeader=" + amzDateHeader);
        logger.trace("dateHeader=" + s3Request.getDate());
        logger.trace("date=" + date);
        logger.trace("url=" + url);
        logger.trace("url.getHost()=" + url.getHost());
        logger.trace("url.getPort()=" + url.getPort());
        if (!StringUtils.equals(authorizationHeader, computedHeader)) {
            logger.info("SIGNATURE_DOES_NOT_MATCH providedHeader=" + authorizationHeader);
            logger.info("SIGNATURE_DOES_NOT_MATCH computedHeader=" + computedHeader);
            throw new S3Exception(ErrorCode.SIGNATURE_DOES_NOT_MATCH);
        }

    }

    private Date getRequestDate(String amzDateHeader) {
        long dateHeader = s3Request.getDate();
        if (dateHeader <= 0 && isBlank(amzDateHeader)) {
            throw new S3Exception(MISSING_SECURITY_HEADER);
        }
        try {
            return isBlank(amzDateHeader) ? new Date(dateHeader) :
                    AWS4Authorization.utcDateFormat(AWS4SignerBase.ISO8601BasicFormat).parse(amzDateHeader);
        } catch (ParseException e) {
            throw new S3Exception(AUTHORIZATION_HEADER_MALFORMED);
        }
    }

    private URI newURIUnchecked(String fullURL) {
        try {
            return new URI(fullURL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private URL newURLUnchecked(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public AWS4SignerForChunkedUpload getAws4SignerForChunkedUpload() {
        if (aws4SignerForChunkedUpload == null) {
            throw new IllegalStateException("Please call verifyHeaders first");
        }
        return aws4SignerForChunkedUpload;
    }
}
