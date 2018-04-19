package us.cuatoi.s34j.spring.auth;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;

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
import static us.cuatoi.s34j.spring.SpringStorageConstants.ALGORITHM;
import static us.cuatoi.s34j.spring.SpringStorageConstants.SCHEME;

@Service
@Rule(name = "AuthorizationHeaderVerifier")
public class AuthorizationHeaderVerifier implements AuthenticationRule {

    public static final Pattern PATTERN = Pattern
            .compile(SCHEME + "-" + ALGORITHM + " Credential=(\\w*)/(\\w*)/([\\w\\-]*)/(\\w*)/aws4_request, SignedHeaders=([\\w;\\-]*), Signature=(\\w*)");
    public static final Logger logger = LoggerFactory.getLogger(AuthorizationHeaderVerifier.class);

    @Condition
    public boolean verifyHeader(Facts facts,
                                @Fact("method") String method,
                                @Fact("path") String path,
                                @Fact("header:authorization") String authorization,
                                @Fact("header:x-amz-date") String xAmzDate,
                                @Fact("header:x-amz-content-sha256") String xAmzContentSha256) {
        logger.debug("verifyHeader() authorization=" + authorization);
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
                .sorted().collect(Collectors.joining(","));
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

        String secretKey = "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG";
        byte[] secret = ("AWS4" + secretKey).getBytes(UTF_8);
        byte[] dateKey = hmacSha256(secret).hashString(dateInScope, UTF_8).asBytes();
        byte[] dateRegionKey = hmacSha256(dateKey).hashString(region, UTF_8).asBytes();
        byte[] dateRegionServiceKey = hmacSha256(dateRegionKey).hashString("s3", UTF_8).asBytes();
        byte[] signingKey = hmacSha256(dateRegionServiceKey).hashString("aws4_request", UTF_8).asBytes();
        String calculatedSignature = hmacSha256(signingKey).hashString(stringToSign, UTF_8).toString();
        logger.debug("verifyHeader() calculatedSignature=" + calculatedSignature);

        if (!equalsIgnoreCase(signature, calculatedSignature)) {
            facts.put("errorCode", ErrorCode.SIGNATURE_DOES_NOT_MATCH);
            return false;
        }

        try {
            Date date = SpringStorageConstants.EXPIRATION_DATE_FORMAT.parse(xAmzDate);
            if (System.currentTimeMillis() - date.getTime() > TimeUnit.HOURS.toMillis(2)) {
                facts.put("errorCode", ErrorCode.REQUEST_TIME_TOO_SKEWED);
                return false;
            }
        } catch (ParseException e) {
            facts.put("errorCode", ErrorCode.AUTHORIZATION_HEADER_MALFORMED);
            return false;
        }

        facts.put("awsAccessKey", awsAccessKey);
        return true;
    }

    @Action
    public void verify(Facts facts) {
        facts.put("authenticated", true);
    }
}
