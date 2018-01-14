package us.cuatoi.s34jserver.core;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.auth.AWS4SignerForChunkedUpload;
import us.cuatoi.s34jserver.core.auth.S3RequestVerifier;
import us.cuatoi.s34jserver.core.model.S3Request;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.S3Constants.CHUNK_SIGNATURE;

public class S3RequestParserVerifier {

    public static final int NEW_LINE_LENGTH = "\r\n".getBytes(StandardCharsets.UTF_8).length;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final S3Context context;
    private final int maxDifferent = Integer.parseInt(System.getProperty("s34j.auth.request.maxDifferenceMinutes", "15")) * 60 * 1000;
    private final S3RequestVerifier verifier;
    private S3Request s3Request;
    private HttpServletRequest request;

    public S3RequestParserVerifier(S3Context context, HttpServletRequest request, S3Request s3Request) {
        this.context = context;
        this.request = request;
        this.s3Request = s3Request;
        this.verifier = new S3RequestVerifier(context, s3Request);
    }

    public S3Request execute() throws Exception {
        parseHeaders();

        if (!equalsIgnoreCase(request.getMethod(), "get")) {
            parseContent();
        }


        //Detect request type
        return new S3RequestDetector(s3Request).detectRequest();
    }

    private void parseContent() throws IOException {
        String contentEncoding = s3Request.getHeader("content-encoding");
        if (equalsIgnoreCase("aws-chunked", contentEncoding)) {
            parseMultipleChunk();
        } else {
            parseSingleChunk();
        }
    }

    private void parseMultipleChunk() throws IOException {
        Path content = Files.createTempFile(s3Request.getRequestId() + ".", ".tmp");
        s3Request.setContent(content);
        AWS4SignerForChunkedUpload signer = verifier.getAws4SignerForChunkedUpload();
        try (OutputStream os = Files.newOutputStream(content)) {
            try (ServletInputStream is = request.getInputStream()) {
                while (!is.isFinished()) {
                    byte[] signatureData = new byte[256];
                    int signatureLength = is.readLine(signatureData, 0, 256);
                    String signatureLine = new String(signatureData, 0, signatureLength);
                    signatureLine = replace(signatureLine, "\r", "");
                    signatureLine = replace(signatureLine, "\n", "");
                    int indexOfSignature = indexOf(signatureLine, CHUNK_SIGNATURE);
                    if (isBlank(signatureLine)) {
                        continue;
                    }
                    if (indexOfSignature <= 0) {
                        throw new S3Exception(ErrorCode.AUTHORIZATION_HEADER_MALFORMED);
                    }
                    logger.trace("Signature Line:" + signatureLine);
                    int chunkSize = Integer.parseUnsignedInt(substring(signatureLine, 0, indexOfSignature), 16);
                    String signature = substring(signatureLine, indexOfSignature + length(CHUNK_SIGNATURE));
                    byte[] data = new byte[chunkSize];
                    if (chunkSize > 0) {
                        IOUtils.readFully(is, data);
                    }
                    String computedSignature = signer.generateChunkSignature(data);
                    logger.trace("signature        =" + signature);
                    logger.trace("computedSignature=" + computedSignature);
                    if (!equalsIgnoreCase(signature, computedSignature)) {
                        throw new S3Exception(ErrorCode.SIGNATURE_DOES_NOT_MATCH);
                    }
                    if (chunkSize == 0) {
                        break;
                    }
                    os.write(data);
                }
            }
        }
        logger.trace("Saved to " + content);
    }

    private long readContentLength() {
        long contentLength = 0;
        try {
            contentLength = Long.parseLong(s3Request.getHeader("content-length"));
        } catch (NumberFormatException ex) {
            throw new S3Exception(ErrorCode.MISSING_CONTENT_LENGTH);
        }
        return contentLength;
    }

    private void parseSingleChunk() throws IOException {
        Path content = Files.createTempFile(s3Request.getRequestId() + ".", ".tmp");
        Files.copy(request.getInputStream(), content, StandardCopyOption.REPLACE_EXISTING);
        s3Request.setContent(content);
        verifier.verifySingleChunk();
    }

    private void parseHeaders() throws URISyntaxException {
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
        for (NameValuePair pair : URLEncodedUtils.parse(new URI(fullURL), UTF_8)) {
            s3Request.setQueryParameter(pair.getName(), pair.getValue());
        }
        verifier.verifyHeaders();
    }
}
