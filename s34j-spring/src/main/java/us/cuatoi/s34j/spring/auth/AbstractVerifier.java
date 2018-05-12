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

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.helper.StorageHelper;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.hash.Hashing.hmacSha256;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34j.spring.SpringStorageConstants.*;

public class AbstractVerifier {
    protected static final Pattern PATTERN = Pattern
            .compile(SCHEME + "-" + ALGORITHM + " Credential=(\\w*)/(\\w*)/([\\w\\-]*)/(\\w*)/aws4_request, SignedHeaders=([\\w;\\-]*), Signature=(\\w*)");
    private static final Logger logger = LoggerFactory.getLogger(AbstractVerifier.class);
    @Autowired
    protected AuthenticationProvider authenticationProvider;
    @Value("${s34j.spring.maxRequestTimeDifferentInSeconds:3600}")
    private int maxRequestTimeDifferentInSeconds;

    protected boolean verifyAuthorizationHeader(Facts facts) {
        String method = facts.get("method");
        String path = facts.get("path");
        String authorization = facts.get("header:authorization");
        String xAmzDate = facts.get("header:x-amz-date");
        String xAmzContentSha256 = facts.get("header:x-amz-content-sha256");
        logger.debug("verifyAuthorizationHeader() method={} path={} authorization={} xAmzDate={} xAmzContentSha256={}",
                method, path, authorization, xAmzDate, xAmzContentSha256);
        Matcher matcher = PATTERN.matcher(authorization);
        if (!matcher.matches()) {
            throw new SpringStorageException(ErrorCode.AUTHORIZATION_HEADER_MALFORMED);
        }
        String awsAccessKey = matcher.group(1);
        logger.debug("verifyHeader() awsAccessKey=" + awsAccessKey);

        String signedHeaderString = matcher.group(5);
        List<String> signedHeaders = Lists.newArrayList(StringUtils.split(signedHeaderString, ";"));
        logger.debug("verifyHeader() signedHeaders=" + signedHeaders);

        String signature = matcher.group(6);
        logger.debug("verifyHeader() signature=" + signature);

        String canonicalQueryString = facts.asMap().keySet().stream()
                .filter((f) -> startsWith(f, "query:"))
                .map((f) -> f + "=" + UrlEscapers.urlFormParameterEscaper().escape(facts.get(f)))
                .map((f) -> replaceFirst(f, "query:", ""))
                .sorted().collect(Collectors.joining("&"));
        String canonicalHeader = facts.asMap().keySet().stream()
                .filter((f) -> startsWith(f, "header:"))
                .filter((f) -> signedHeaders.contains(replaceFirst(f, "header:", "")))
                .map((f) -> f + ":" + trim(facts.get(f)))
                .map((f) -> replaceFirst(f, "header:", ""))
                .sorted().collect(Collectors.joining("\n"));

        String canonicalRequest = upperCase(method) + "\n";
        canonicalRequest += UrlEscapers.urlFragmentEscaper().escape(path) + "\n";
        canonicalRequest += canonicalQueryString + "\n";
        canonicalRequest += canonicalHeader + "\n\n";
        canonicalRequest += signedHeaderString + "\n";
        canonicalRequest += xAmzContentSha256;
        logger.debug("verifyHeader() canonicalRequest=\n" + canonicalRequest);

        String canonicalRequestHash = Hashing.sha256().hashString(canonicalRequest, UTF_8).toString();
        String dateInScope = matcher.group(2);
        String region = matcher.group(3);

        String stringToSign = SCHEME + "-" + ALGORITHM + "\n";
        stringToSign += xAmzDate + "\n";
        stringToSign += dateInScope + "/" + region + "/s3/aws4_request" + "\n";
        stringToSign += canonicalRequestHash;
        logger.debug("verifyHeader() stringToSign=\n" + stringToSign);
        String secretKey = StringUtils.trimToEmpty(authenticationProvider.getSecretKey(awsAccessKey));
        byte[] secret = ("AWS4" + secretKey).getBytes(UTF_8);
        byte[] dateKey = hmacSha256(secret).hashString(dateInScope, UTF_8).asBytes();
        byte[] dateRegionKey = hmacSha256(dateKey).hashString(region, UTF_8).asBytes();
        byte[] dateRegionServiceKey = hmacSha256(dateRegionKey).hashString(SpringStorageConstants.SERVICE, UTF_8).asBytes();
        byte[] signingKey = hmacSha256(dateRegionServiceKey).hashString(SpringStorageConstants.TERMINATOR, UTF_8).asBytes();
        String calculatedSignature = hmacSha256(signingKey).hashString(stringToSign, UTF_8).toString();
        logger.debug("verifyHeader() calculatedSignature={} signature={}",
                calculatedSignature, signature);

        if (isBlank(secretKey)) {
            facts.put("errorCode", ErrorCode.INVALID_ACCESS_KEY_ID);
            return false;
        }

        if (!equalsIgnoreCase(signature, calculatedSignature)) {
            facts.put("errorCode", ErrorCode.SIGNATURE_DOES_NOT_MATCH);
            return false;
        }

        try {
            Date date = StorageHelper.parse(X_AMZ_DATE_FORMAT, xAmzDate);
            if (System.currentTimeMillis() - date.getTime() > TimeUnit.SECONDS.toMillis(maxRequestTimeDifferentInSeconds)) {
                facts.put("errorCode", ErrorCode.REQUEST_TIME_TOO_SKEWED);
                return false;
            }
        } catch (ParseException e) {
            facts.put("errorCode", ErrorCode.AUTHORIZATION_HEADER_MALFORMED);
            return false;
        }

        facts.put("region", region);
        facts.put("awsAccessKey", awsAccessKey);
        return true;
    }
}
