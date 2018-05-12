/*
 * Copyright (C) 2018 dungviettran89@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package us.cuatoi.s34j.spring.auth;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.annotation.*;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.hash.Hashing.hmacSha256;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34j.spring.dto.ErrorCode.AUTHORIZATION_HEADER_MALFORMED;
import static us.cuatoi.s34j.spring.dto.ErrorCode.SIGNATURE_DOES_NOT_MATCH;

@Service
@Rule(name = "ChunkedUploadVerifier")
public class ChunkedUploadVerifier extends AbstractVerifier implements AuthenticationRule {

    public static final Logger logger = LoggerFactory.getLogger(ChunkedUploadVerifier.class);

    @Priority
    public int priority() {
        return 1;
    }

    @Condition
    public boolean shouldVerify(Facts facts, @Fact("header:content-encoding") String contentEncoding,
                                @Fact("header:x-amz-content-sha256") String xAmzContentSha256,
                                @Fact("header:authorization") String authorization) {
        boolean isMultipleChunk = contains(contentEncoding, "aws-chunked") &&
                equalsIgnoreCase(xAmzContentSha256, SpringStorageConstants.STREAMING_PAYLOAD);
        if (!isMultipleChunk) {
            return false;
        }
        return verifyAuthorizationHeader(facts);
    }

    @Action
    public void verify(Facts facts, @Fact("header:authorization") String authorization, @Fact("requestInputStream") InputStream requestInputStream) {
        Matcher matcher = PATTERN.matcher(authorization);
        if (!matcher.matches()) {
            logger.warn("Authorization header doesn't match. authorization={}", authorization);
            return;
        }
        String xAmzDate = facts.get("header:x-amz-date");
        String awsAccessKey = matcher.group(1);
        String dateInScope = matcher.group(2);
        String region = matcher.group(3);
        String previousSignature = matcher.group(6);
        String secretKey = StringUtils.trimToEmpty(authenticationProvider.getSecretKey(awsAccessKey));
        byte[] secret = ("AWS4" + secretKey).getBytes(UTF_8);
        byte[] dateKey = hmacSha256(secret).hashString(dateInScope, UTF_8).asBytes();
        byte[] dateRegionKey = hmacSha256(dateKey).hashString(region, UTF_8).asBytes();
        byte[] dateRegionServiceKey = hmacSha256(dateRegionKey).hashString(SpringStorageConstants.SERVICE, UTF_8).asBytes();
        byte[] signingKey = hmacSha256(dateRegionServiceKey).hashString(SpringStorageConstants.TERMINATOR, UTF_8).asBytes();

        facts.put("authenticated", true);
        facts.put("requestInputStream", new ChunkedDecodeInputStream(requestInputStream, signingKey,
                previousSignature, xAmzDate, dateInScope, region));
    }
}

class ChunkedDecodeInputStream extends InputStream {


    public static final Pattern CHUNK_HEADER_PATTERN = Pattern.compile("(\\w+);chunk-signature=(\\w+)");
    public static final Logger logger = LoggerFactory.getLogger(ChunkedUploadVerifier.class);
    private final byte[] signingKey;
    private final String xAmzDate;
    private final String dateInScope;
    private final String blankHash;
    private final InputStream inputStream;
    private final String region;
    private byte[] buffer = null;
    private int index;
    private String previousSignature;

    public ChunkedDecodeInputStream(InputStream inputStream, byte[] signingKey,
                                    String previousSignature, String xAmzDate, String dateInScope, String region) {
        this.inputStream = new BufferedInputStream(inputStream, 64 * 1024);
        this.signingKey = signingKey;
        this.previousSignature = previousSignature;
        this.xAmzDate = xAmzDate;
        this.dateInScope = dateInScope;
        blankHash = Hashing.sha256().hashString("", UTF_8).toString();
        this.region = region;
    }

    public static String readLine(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int c;
        for (c = inputStream.read(); c != '\n' && c != -1; c = inputStream.read()) {
            if (c != '\r') stringBuilder.append((char) c);
        }
        if (c == -1 && stringBuilder.length() == 0) return null; // End of stream and nothing to return
        return stringBuilder.toString();
    }

    @Override
    public int read() throws IOException {
        if (buffer != null && index < buffer.length) {
            return buffer[index++];
        }
        String signatureLine = readLine(inputStream);
        if (signatureLine == null) {
            return -1;
        }
        Matcher matcher = CHUNK_HEADER_PATTERN.matcher(signatureLine);
        if (!matcher.matches()) {
            throw new SpringStorageException(AUTHORIZATION_HEADER_MALFORMED);
        }
        int chunkSize = Integer.parseInt(matcher.group(1), 16);
        if (chunkSize == 0) {
            return -1;
        }
        String chunkSignature = matcher.group(2);
        buffer = new byte[chunkSize];
        try {
            ByteStreams.readFully(inputStream, buffer);
        } catch (EOFException exception) {
            logger.warn("Can not fill buffer, chunkSize={}", chunkSize, exception);
            throw new SpringStorageException(ErrorCode.INCOMPLETE_BODY);
        }
        String chunkStringToSign = "AWS4-HMAC-SHA256-PAYLOAD" + "\n";
        chunkStringToSign += xAmzDate + "\n";
        chunkStringToSign += dateInScope + "/" + region + "/s3/aws4_request" + "\n";
        chunkStringToSign += previousSignature + "\n";
        chunkStringToSign += blankHash + "\n";
        chunkStringToSign += Hashing.sha256().hashBytes(buffer).toString();
        String calculatedChunkSignature = hmacSha256(signingKey).hashString(chunkStringToSign, UTF_8).toString();
        if (!equalsIgnoreCase(calculatedChunkSignature, chunkSignature)) {
            throw new SpringStorageException(SIGNATURE_DOES_NOT_MATCH);
        }
        previousSignature = calculatedChunkSignature;
        String blankLine = readLine(inputStream);
        if (isNotBlank(blankLine)) {
            logger.error("Must be a blank line. blankLine={}", blankLine);
            throw new SpringStorageException(SIGNATURE_DOES_NOT_MATCH);
        }
        index = 0;
        return buffer[index++];
    }
}
