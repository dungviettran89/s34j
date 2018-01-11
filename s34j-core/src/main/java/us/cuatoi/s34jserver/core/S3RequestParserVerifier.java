package us.cuatoi.s34jserver.core;

import com.google.common.io.BaseEncoding;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.auth.AWS4SignerForChunkedUpload;
import us.cuatoi.s34jserver.core.auth.S3RequestVerifier;
import us.cuatoi.s34jserver.core.model.S3Request;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.S3Constants.CHUNK_SIGNATURE;

public class S3RequestParserVerifier {

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
        long contentLength = readContentLength();
        Path content = Files.createTempFile(s3Request.getRequestId() + ".", ".tmp");
        s3Request.setContent(content);
        AWS4SignerForChunkedUpload signer = verifier.getAws4SignerForChunkedUpload();
        try (OutputStream os = Files.newOutputStream(content)) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
                String signatureLine = null;
                boolean lastChunk = false;
                do {
                    signatureLine = br.readLine();
                    if (lastChunk && isNotBlank(signatureLine)) {
                        throw new S3Exception(ErrorCode.INCOMPLETE_BODY);
                    }
                    int indexOfSignature = indexOf(signatureLine, CHUNK_SIGNATURE);
                    if (indexOfSignature > 0) {
                        logger.debug("Signature Line:" + signatureLine);
                        int chunkSize = Integer.parseUnsignedInt(substring(signatureLine, 0, indexOfSignature), 16);
                        String signature = substring(signatureLine, indexOfSignature + length(CHUNK_SIGNATURE));
                        byte[] data = BaseEncoding.base64().decode(br.readLine());
                        lastChunk = data.length < chunkSize;
                        String computedSignature = signer.generateChunkSignature(data);
                        if (!equalsIgnoreCase(signature, computedSignature)) {
                            throw new S3Exception(ErrorCode.SIGNATURE_DOES_NOT_MATCH);
                        }
                        os.write(data);
                    }
                } while (isNotBlank(signatureLine));
            }
        }
        if (Files.size(content) != contentLength) {
            throw new S3Exception(ErrorCode.INCOMPLETE_BODY);
        }
        throw new S3Exception(ErrorCode.NOT_IMPLEMENTED);
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
