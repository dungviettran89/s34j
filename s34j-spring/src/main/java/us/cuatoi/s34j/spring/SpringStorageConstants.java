package us.cuatoi.s34j.spring;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class SpringStorageConstants {
    public static final String EMPTY_BODY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
    public static final String SCHEME = "AWS4";
    public static final String ALGORITHM = "HMAC-SHA256";
    public static final String TERMINATOR = "aws4_request";
    public static final String ISO8601BasicFormat = "yyyyMMdd'T'HHmmss'Z'";
    public static final String DateStringFormat = "yyyyMMdd";

    public static final String CONTENT_TYPE_XML = "application/xml; charset=utf-8";
    public static final String WORK_DIR = ".s34j";
    public static final String METADATA_DIR = "metadata";
    public static final String UPLOAD_DIR = "upload";

    public static final String METADATA_JSON = "metadata.json";
    public static final String CHUNK_SIGNATURE = ";chunk-signature=";
    public static final String STORAGE_CLASS = "STANDARD";
    public static final String ETAG_SUFFIX = ".etag";
    public static final String POLICY_JSON = "policy.json";
    public static final SimpleDateFormat EXPIRATION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH':'mm':'ss'.'SSS'Z'", Locale.US);
    public static final SimpleDateFormat HTTP_HEADER_DATE_FORMAT = new SimpleDateFormat("EEE',' dd MMM yyyy HH':'mm':'ss 'GMT'", Locale.US);
    public static final SimpleDateFormat AUTHORIZATION_HEADER_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);

    static {
        EXPIRATION_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        HTTP_HEADER_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        AUTHORIZATION_HEADER_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
}
