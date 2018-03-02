package us.cuatoi.s34jserver.core.servlet;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.auth.AWS4Authorization;
import us.cuatoi.s34jserver.core.auth.AWS4SignerBase;
import us.cuatoi.s34jserver.core.auth.AWS4SignerForAuthorizationHeader;
import us.cuatoi.s34jserver.core.auth.AWS4SignerForChunkedUpload;
import us.cuatoi.s34jserver.core.helper.PathHelper;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.ErrorCode.AUTHORIZATION_HEADER_MALFORMED;
import static us.cuatoi.s34jserver.core.ErrorCode.MISSING_SECURITY_HEADER;
import static us.cuatoi.s34jserver.core.auth.AWS4SignerForChunkedUpload.STREAMING_BODY_SHA256;
import static us.cuatoi.s34jserver.core.helper.PathHelper.md5HashFileToByte;

public class ServletParserVerifier {

    private final StorageContext context;
    private final Request request;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AWS4SignerForChunkedUpload aws4SignerForChunkedUpload;


    public ServletParserVerifier(StorageContext context, Request request) {
        this.context = context;
        this.request = request;
    }

    public void verifyHeaders() throws Exception {
        URL url = new URL(URLDecoder.decode(request.getUrl(), "UTF-8"));

        String authorizationHeader = request.getHeader("authorization");
        if (isBlank(authorizationHeader)) {
            String algorithm = request.getQueryParameter("X-Amz-Algorithm");
            String credential = request.getQueryParameter("X-Amz-Credential");
            String date = request.getQueryParameter("X-Amz-Date");
            String signedHeaders = request.getQueryParameter("X-Amz-SignedHeaders");
            String signature = request.getQueryParameter("X-Amz-Signature");
            if (isNoneBlank(algorithm, credential, date, signedHeaders, signature)) {
                authorizationHeader = algorithm +
                        " Credential=" + credential +
                        ", SignedHeaders=" + signedHeaders +
                        ", Signature=" + signature;
                logger.debug("Constructed authorizationHeader based on Query String:" + authorizationHeader);
            }
        }
        if (isBlank(authorizationHeader) && equalsIgnoreCase(request.getMethod(), "post")) {
            String algorithm = request.getFormParameter("x-amz-algorithm");
            String credential = request.getFormParameter("x-amz-credential");
            String date = request.getFormParameter("x-amz-date");
            String signedHeaders = "host";
            String signature = request.getFormParameter("x-amz-signature");
            if (isNoneBlank(algorithm, credential, date, signature)) {
                authorizationHeader = algorithm +
                        " Credential=" + credential +
                        ", SignedHeaders=" + signedHeaders +
                        ", Signature=" + signature;
                logger.debug("Constructed authorizationHeader based on Form Data:" + authorizationHeader);
            }
        }
        if (isBlank(authorizationHeader)) {
            logger.info("MISSING_SECURITY_HEADER authorizationHeader=" + authorizationHeader);
            logger.debug("MISSING_SECURITY_HEADER request=" + request);
            throw new S3Exception(MISSING_SECURITY_HEADER);
        }

        String amzDateHeader = request.getHeader("x-amz-date");
        if (isBlank(amzDateHeader)) {
            amzDateHeader = request.getQueryParameter("X-Amz-Date");
        }
        if (isBlank(amzDateHeader)) {
            amzDateHeader = request.getFormParameter("x-amz-date");
        }
        Date date = getRequestDate(amzDateHeader);

        AWS4Authorization authorization = new AWS4Authorization(authorizationHeader);
        String bodyHash = request.getHeader("x-amz-content-sha256");
        if (isBlank(bodyHash)) {
            bodyHash = "UNSIGNED-PAYLOAD";
        }

        String method = request.getMethod();
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

        //Handle HTTP Post
        if (equalsIgnoreCase(request.getMethod(), "post") &&
                contains(request.getHeader("content-type"), "multipart/form-data")) {
            String computedHTTPPostSignature = signer.signPOSTPolicy(awsSecretKey, date, request.getFormParameter("policy"));
            String providedHTTPPostSignature = request.getFormParameter("x-amz-signature");
            if (!equalsIgnoreCase(computedHTTPPostSignature, providedHTTPPostSignature)) {
                logger.info("SIGNATURE_DOES_NOT_MATCH computedHTTPPostSignature=" + computedHTTPPostSignature);
                logger.info("SIGNATURE_DOES_NOT_MATCH providedHTTPPostSignature=" + providedHTTPPostSignature);
                throw new S3Exception(ErrorCode.SIGNATURE_DOES_NOT_MATCH);
            }
            return;
        }

        //Handle normal request
        HashMap<String, String> headers = new HashMap<>();
        for (String header : authorization.getSignedHeaders()) {
            if (!equalsAnyIgnoreCase(header, "host")) {
                headers.put(header, request.getHeader(header));
            }
        }

        HashMap<String, String> queryParams = new HashMap<>();
        request.getQueryParameters().forEach((k, v) -> {
            if (!equalsIgnoreCase(k, "X-Amz-Signature")) {
                queryParams.put(k, v);
            }
        });

        Date now = new Date();
        String expiresParams = request.getQueryParameter("X-Amz-Expires");
        if (isNotBlank(expiresParams)) {
            int expires = Integer.parseInt(expiresParams) * 1000;
            if (Math.abs(date.getTime() - now.getTime()) > expires) {
                logger.info("EXPIRED_TOKEN currentDate=" + now);
                logger.info("EXPIRED_TOKEN requestDate=" + date);
                throw new S3Exception(ErrorCode.EXPIRED_TOKEN);
            }
        } else {
            if (Math.abs(date.getTime() - now.getTime()) > S3Context.MAX_DIFFERENT_IN_REQUEST_TIME) {
                logger.info("REQUEST_TIME_TOO_SKEWED currentDate=" + now);
                logger.info("REQUEST_TIME_TOO_SKEWED requestDate=" + date);
                throw new S3Exception(ErrorCode.REQUEST_TIME_TOO_SKEWED);
            }
        }

        String computedHeader = signer.computeSignature(headers, queryParams, bodyHash, awsAccessKey, awsSecretKey, date);
        aws4SignerForChunkedUpload.computeSignature(headers, queryParams, STREAMING_BODY_SHA256, awsAccessKey, awsSecretKey, date);
        logger.trace("headers=" + headers);
        logger.trace("parameters=" + queryParams);
        logger.trace("bodyHash=" + bodyHash);
        logger.trace("amzDateHeader=" + amzDateHeader);
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
        long dateHeader = request.getDate();
        if (dateHeader <= 0 && isBlank(amzDateHeader)) {
            logger.info("MISSING_SECURITY_HEADER amzDateHeader=" + amzDateHeader);
            throw new S3Exception(MISSING_SECURITY_HEADER);
        }
        try {
            return isBlank(amzDateHeader) ? new Date(dateHeader) :
                    AWS4Authorization.utcDateFormat(AWS4SignerBase.ISO8601BasicFormat).parse(amzDateHeader);
        } catch (ParseException e) {
            logger.info("AUTHORIZATION_HEADER_MALFORMED amzDateHeader=" + amzDateHeader);
            throw new S3Exception(AUTHORIZATION_HEADER_MALFORMED);
        }
    }

    public void verifySingleChunk() throws IOException {
        String providedSha256 = request.getHeader("x-amz-content-sha256");
        Path content = request.getContent();
        logger.trace("Checking " + content);
        long contentLength = Files.size(content);
        if (isNotBlank(providedSha256)) {
            String computedSha256 = contentLength > 0 ? PathHelper.sha256HashFile(content) : AWS4SignerBase.EMPTY_BODY_SHA256;
            if (!equalsIgnoreCase(computedSha256, providedSha256)) {
                logger.info("X_AMZ_CONTENT_SHA256_MISMATCH: providedSha256=" + providedSha256);
                logger.info("X_AMZ_CONTENT_SHA256_MISMATCH: computedSha256=" + computedSha256);
                throw new S3Exception(ErrorCode.X_AMZ_CONTENT_SHA256_MISMATCH);
            }
        }
        String providedMd5 = request.getHeader("content-md5");
        if (contentLength > 0 && isNotBlank(providedMd5)) {
            String computedMd5 = BaseEncoding.base64().encode(md5HashFileToByte(content));
            if (!equalsIgnoreCase(providedMd5, computedMd5)) {
                logger.info("INVALID_DIGEST: providedMd5=" + providedMd5);
                logger.info("INVALID_DIGEST: computedMd5=" + computedMd5);
                throw new S3Exception(ErrorCode.INVALID_DIGEST);
            }
        }
        
    }

    public AWS4SignerForChunkedUpload getAws4SignerForChunkedUpload() {
        return aws4SignerForChunkedUpload;
    }
}
