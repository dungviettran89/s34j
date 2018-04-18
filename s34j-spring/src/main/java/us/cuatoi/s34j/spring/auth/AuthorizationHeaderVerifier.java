package us.cuatoi.s34j.spring.auth;

import com.google.common.collect.Lists;
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
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34j.spring.SpringStorageConstant.ALGORITHM;
import static us.cuatoi.s34j.spring.SpringStorageConstant.SCHEME;

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
                                @Fact("header:x-amz-content-sha256") String contentSha256) {
        logger.debug("verifyHeader() authorization=" + authorization);
        Matcher matcher = PATTERN.matcher(authorization);
        if (!matcher.matches()) {
            throw new SpringStorageException(ErrorCode.AUTHORIZATION_HEADER_MALFORMED);
        }
        String awsAccessKey = matcher.group(1);
        logger.debug("verifyHeader() awsAccessKey=" + awsAccessKey);

        List<String> signedHeaders = Lists.newArrayList(StringUtils.split(matcher.group(5), ";"));
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
                .map((f) -> f + ":" + facts.get(f))
                .map((f) -> replaceFirst(f, "header:", ""))
                .sorted().collect(Collectors.joining("\n"));

        String canonicalRequest = upperCase(method) + "\n";
        canonicalRequest += UrlEscapers.urlFragmentEscaper().escape(path) + "\n";
        canonicalRequest += canonicalQueryString + "\n";
        canonicalRequest += canonicalHeader + "\n";
        canonicalRequest += contentSha256;
        logger.debug("verifyHeader() canonicalRequest=\n" + canonicalRequest);

        return false;
    }

    @Action
    public void verify(Facts facts) {

    }
}
