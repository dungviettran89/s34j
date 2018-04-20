package us.cuatoi.s34j.spring;

public class SpringStorageConstants {
    public static final String STREAMING_PAYLOAD = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD";
    public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
    public static final String SCHEME = "AWS4";
    public static final String ALGORITHM = "HMAC-SHA256";
    public static final String SERVICE = "s3";
    public static final String TERMINATOR = "aws4_request";

    public static final String EXPIRATION_DATE_FORMAT = "yyyy-MM-dd'T'HH':'mm':'ss'.'SSS'Z'";
    public static final String X_AMZ_DATE_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
    public static final String EEE_DD_MMM_YYYY_HH_MM_SS_GMT = "EEE',' dd MMM yyyy HH':'mm':'ss 'GMT'";
    public static final String SCOPE_DATE_FORMAT = "yyyyMMdd";

}