package us.cuatoi.s34j.spring;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class SpringStorageConstants {
    public static final String STREAMING_PAYLOAD = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD";
    public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
    public static final String SCHEME = "AWS4";
    public static final String ALGORITHM = "HMAC-SHA256";
    public static final String SERVICE = "s3";
    public static final String TERMINATOR = "aws4_request";


    public static final SimpleDateFormat EXPIRATION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH':'mm':'ss'.'SSS'Z'", Locale.US);
    public static final SimpleDateFormat HTTP_HEADER_DATE_FORMAT = new SimpleDateFormat("EEE',' dd MMM yyyy HH':'mm':'ss 'GMT'", Locale.US);
    public static final SimpleDateFormat SCOPE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);
    public static final SimpleDateFormat X_AMZ_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);

    static {
        EXPIRATION_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        HTTP_HEADER_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        SCOPE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        X_AMZ_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
}
