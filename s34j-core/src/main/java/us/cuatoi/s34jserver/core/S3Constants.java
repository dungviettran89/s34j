package us.cuatoi.s34jserver.core;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class S3Constants {
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

    public static final DateTimeFormatter EXPIRATION_DATE_FORMAT =
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH':'mm':'ss'.'SSS'Z'").withZoneUTC().withLocale(Locale.US);
    public static final DateTimeFormatter HTTP_HEADER_DATE_FORMAT =
            DateTimeFormat.forPattern("EEE',' dd MMM yyyy HH':'mm':'ss 'GMT'").withZoneUTC().withLocale(Locale.US);
    public static final String METADATA_JSON = "metadata.json";
    public static final String CHUNK_SIGNATURE = ";chunk-signature=";
    public static final String STORAGE_CLASS = "STANDARD";
    public static final String ETAG_SUFFIX = ".etag";
    public static final String POLICY_JSON = "policy.json";
}
