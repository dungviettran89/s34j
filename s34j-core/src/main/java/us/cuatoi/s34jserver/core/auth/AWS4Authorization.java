package us.cuatoi.s34jserver.core.auth;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Exception;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static us.cuatoi.s34jserver.core.S3Constants.*;

public class AWS4Authorization {
    public static final Pattern PATTERN = Pattern
            .compile(SCHEME + "-" + ALGORITHM + " Credential=(\\w*)/(\\w*)/([\\w\\-]*)/(\\w*)/aws4_request, SignedHeaders=([\\w;\\-]*), Signature=(\\w*)");
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String awsAccessKey;
    private Date date;
    private String regionName;
    private String serviceName;
    private List<String> signedHeaders = Lists.newArrayList();
    private String signature;


    public AWS4Authorization() {
    }

    public AWS4Authorization(String header) {
        fromString(header);
    }


    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public AWS4Authorization setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public AWS4Authorization setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getRegionName() {
        return regionName;
    }

    public AWS4Authorization setRegionName(String regionName) {
        this.regionName = regionName;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public AWS4Authorization setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getSignature() {
        return signature;
    }

    public AWS4Authorization setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public List<String> getSignedHeaders() {
        return signedHeaders;
    }

    public AWS4Authorization setSignedHeaders(List<String> signedHeaders) {
        this.signedHeaders = signedHeaders;
        return this;
    }


    public AWS4Authorization fromString(String authorizationHeader) {
        Matcher matcher = PATTERN.matcher(authorizationHeader);

        if (!matcher.matches()) {
            throw new S3Exception(ErrorCode.AUTHORIZATION_HEADER_MALFORMED);
        }

        try {
            awsAccessKey = matcher.group(1);
            date = utcDateFormat(DateStringFormat).parse(matcher.group(2));
            regionName = matcher.group(3);
            serviceName = matcher.group(4);
            signedHeaders = Lists.newArrayList(StringUtils.split(matcher.group(5), ";"));
            signature = matcher.group(6);
            return this;
        } catch (Exception ex) {
            logger.warn("Can not parse header " + authorizationHeader, ex);
            throw new S3Exception(ErrorCode.AUTHORIZATION_HEADER_MALFORMED);
        }
    }

    public static SimpleDateFormat utcDateFormat(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
        return dateFormat;
    }

    @Override
    public String toString() {
        String credential = awsAccessKey + "/" + utcDateFormat(DateStringFormat).format(date) + "/" + regionName + "/" + serviceName + "/" + TERMINATOR;
        String headerNames = getCanonicalizeHeaderNames(signedHeaders);
        return SCHEME + "-" + ALGORITHM + " "
                + "Credential=" + credential + ", "
                + "SignedHeaders=" + headerNames + ", "
                + "Signature=" + signature;
    }

    public static String getCanonicalizeHeaderNames(List<String> headerNames) {
        List<String> sortedHeaders = new ArrayList<String>();
        sortedHeaders.addAll(headerNames);
        Collections.sort(sortedHeaders, String.CASE_INSENSITIVE_ORDER);

        StringBuilder buffer = new StringBuilder();
        for (String header : sortedHeaders) {
            if (buffer.length() > 0) buffer.append(";");
            buffer.append(header.toLowerCase());
        }

        return buffer.toString();
    }
}
