package us.cuatoi.s34j.spring.dto;

import static javax.servlet.http.HttpServletResponse.*;

public enum ErrorCode {
    // custom error codes
    NO_SUCH_OBJECT(SC_NOT_FOUND, "NoSuchKey", "Object does not exist"),
    RESOURCE_NOT_FOUND(SC_NOT_FOUND, "ResourceNotFound", "Request resource not found"),
    RESOURCE_CONFLICT(SC_CONFLICT, "ResourceConflict", "Request resource conflicts"),

    // S3 error codes
    ACCESS_DENIED(SC_FORBIDDEN, "AccessDenied", "Access denied"),
    ACCOUNT_PROBLEM(SC_FORBIDDEN, "AccountProblem", "Problem with provided account"),
    AMBIGUOUS_GRANT_BY_EMAIL_ADDRESS(SC_BAD_REQUEST, "AmbiguousGrantByEmailAddress",
            "The email address you provided is associated with more than one account"),
    AUTHORIZATION_HEADER_MALFORMED(SC_BAD_REQUEST, "AuthorizationHeaderMalformed", "The authorization header is malformed"),
    BAD_DIGEST(SC_BAD_REQUEST, "BadDigest", "Specified Content-MD5 does not match"),
    BUCKET_ALREADY_EXISTS(SC_CONFLICT, "BucketAlreadyExists", "Bucket already exists"),
    BUCKET_ALREADY_OWNED_BY_YOU(SC_CONFLICT, "BucketAlreadyOwnedByYou", "Bucket is already owned by you"),
    BUCKET_NOT_EMPTY(SC_CONFLICT, "BucketNotEmpty", "Bucket is not empty"),
    CREDENTIALS_NOT_SUPPORTED(SC_BAD_REQUEST, "CredentialsNotSupported", "Request does not support credentials"),
    CROSS_LOCATION_LOGGING_PROHIBITED(SC_CONFLICT, "CrossLocationLoggingProhibited", "Cross-location logging not allowed"),
    ENTITY_TOO_SMALL(SC_BAD_REQUEST, "EntityTooSmall", "Upload is smaller than the minimum allowed object size"),
    ENTITY_TOO_LARGE(SC_BAD_REQUEST, "EntityTooLarge", "Upload exceeds the maximum allowed object size"),
    EXPIRED_TOKEN(SC_BAD_REQUEST, "ExpiredToken", "The provided token has expired"),
    ILLEGAL_VERSIONING_CONFIGURATION_EXCEPTION(SC_BAD_REQUEST, "IllegalVersioningConfigurationException",
            "The versioning configuration specified in the request is invalid."),
    INCOMPLETE_BODY(SC_BAD_REQUEST, "IncompleteBody", "HTTP body size does not match with the Content-Length HTTP header"),
    INCORRECT_NUMBER_OF_FILES_IN_POST_REQUEST(SC_BAD_REQUEST, "IncorrectNumberOfFilesInPostRequest",
            "POST requires exactly one file upload per request"),
    INLINE_DATA_TOO_LARGE(SC_BAD_REQUEST, "InlineDataTooLarge", "Inline data exceeds the maximum allowed size"),
    INTERNAL_ERROR(SC_INTERNAL_SERVER_ERROR, "InternalError", "Internal error. Please try again"),
    INVALID_ACCESS_KEY_ID(SC_FORBIDDEN, "InvalidAccessKeyId", "access key does not exist"),
    INVALID_ADDRESSING_HEADER(SC_OK, "InvalidAddressingHeader", "Invalid addressing header.  Specify the Anonymous role"),
    INVALID_ARGUMENT(SC_BAD_REQUEST, "InvalidArgument", "Invalid Argument"),
    INVALID_BUCKET_NAME(SC_BAD_REQUEST, "InvalidBucketName", "Bucket name is not valid"),
    INVALID_BUCKET_STATE(SC_CONFLICT, "InvalidBucketState", "The request is not valid with the current state of the bucket"),
    INVALID_DIGEST(SC_BAD_REQUEST, "InvalidDigest", "Specified Content-MD5 is not valid"),
    INVALID_ENCRYPTION_ALGORITHM_ERROR(SC_BAD_REQUEST, "InvalidEncryptionAlgorithmError", "Invalid encryption algorithm error"),
    INVALID_LOCATION_CONSTRAINT(SC_BAD_REQUEST, "InvalidLocationConstraint", "The specified location constraint is not valid"),
    INVALID_OBJECT_STATE(SC_CONFLICT, "InvalidObjectState", "The operation is not valid for the current state of the object"),
    INVALID_PART(SC_BAD_REQUEST, "InvalidPart", "One or more of the specified parts could not be found"),
    INVALID_PART_ORDER(SC_BAD_REQUEST, "InvalidPartOrder", "The list of parts was not in ascending order.  "
            + "Parts list must specified in order by part number"),
    INVALID_PAYER(SC_FORBIDDEN, "InvalidPayer", "All access to this object has been disabled"),
    INVALID_POLICY_DOCUMENT(SC_BAD_REQUEST, "InvalidPolicyDocument",
            "The content of the form does not meet the conditions specified in the policy document"),
    INVALID_RANGE(SC_REQUESTED_RANGE_NOT_SATISFIABLE, "InvalidRange", "The requested range cannot be satisfied"),
    INVALID_REQUEST(SC_BAD_REQUEST, "InvalidRequest", "SOAP requests must be made over an HTTPS connection"),
    INVALID_SECURITY(SC_FORBIDDEN, "InvalidSecurity", "The provided security credentials are not valid"),
    INVALID_SOAP_REQUEST(SC_BAD_REQUEST, "InvalidSOAPRequest", "The SOAP request body is invalid"),
    INVALID_STORAGE_CLASS(SC_BAD_REQUEST, "InvalidStorageClass", "The storage class you specified is not valid"),
    INVALID_TARGET_BUCKET_FOR_LOGGING(SC_BAD_REQUEST, "InvalidTargetBucketForLogging",
            "The target bucket for logging does not exist, is not owned by you, or does not "
                    + "have the appropriate grants for the log-delivery group."),
    INVALID_TOKEN(SC_BAD_REQUEST, "InvalidToken", "malformed or invalid token"),
    INVALID_URI(SC_BAD_REQUEST, "InvalidURI", "Couldn't parse the specified URI"),
    KEY_TOO_LONG(SC_BAD_REQUEST, "KeyTooLong", "Key is too long"),
    MALFORMED_ACL_ERROR(SC_BAD_REQUEST, "MalformedACLError",
            "The XML provided was not well-formed or did not validate against published schema"),
    MALFORMED_POST_REQUEST(SC_BAD_REQUEST, "MalformedPOSTRequest", "The body of POST request is not well-formed multipart/form-data"),
    MALFORMED_XML(SC_BAD_REQUEST, "MalformedXML", "Malformed XML"),
    MAX_MESSAGE_LENGTH_EXCEEDED(SC_BAD_REQUEST, "MaxMessageLengthExceeded", "Request was too big"),
    MAX_POST_PRE_DATA_LENGTH_EXCEEDED_ERROR(SC_BAD_REQUEST, "MaxPostPreDataLengthExceededError",
            "POST request fields preceding the upload file were too large"),
    METADATA_TOO_LARGE(SC_BAD_REQUEST, "MetadataTooLarge", "Metadata headers exceed the maximum allowed metadata size"),
    METHOD_NOT_ALLOWED(SC_METHOD_NOT_ALLOWED, "MethodNotAllowed", "The specified method is not allowed against this resource"),
    MISSING_ATTACHMENT(SC_BAD_REQUEST, "MissingAttachment", "A SOAP attachment was expected, but none were found"),
    MISSING_CONTENT_LENGTH(SC_LENGTH_REQUIRED, "MissingContentLength", "missing the Content-Length HTTP header"),
    MISSING_REQUEST_BODY_ERROR(SC_BAD_REQUEST, "MissingRequestBodyError", "Request body is empty"),
    MISSING_SECURITY_ELEMENT(SC_BAD_REQUEST, "MissingSecurityElement", "The SOAP 1.1 request is missing a security element"),
    MISSING_SECURITY_HEADER(SC_BAD_REQUEST, "MissingSecurityHeader", "Request is missing a required header"),
    NO_LOGGING_STATUS_FOR_KEY(SC_BAD_REQUEST, "NoLoggingStatusForKey",
            "There is no such thing as a logging status subresource for a key"),
    NO_SUCH_BUCKET(SC_NOT_FOUND, "NoSuchBucket", "Bucket does not exist"),
    NO_SUCH_KEY(SC_NOT_FOUND, "NoSuchKey", "Object does not exist"),
    NO_SUCH_LIFECYCLE_CONFIGURATION(SC_NOT_FOUND, "NoSuchLifecycleConfiguration", "The lifecycle configuration does not exist"),
    NO_SUCH_UPLOAD(SC_NOT_FOUND, "NoSuchUpload", "Multipart upload does not exist"),
    NO_SUCH_VERSION(SC_NOT_FOUND, "NoSuchVersion", "Specified version ID does not match an existing version"),
    NOT_IMPLEMENTED(SC_NOT_IMPLEMENTED, "NotImplemented", "A header you provided implies functionality that is not implemented."),
    NOT_SIGNED_UP(SC_FORBIDDEN, "NotSignedUp", "Account is not signed up"),
    NO_SUCH_BUCKET_POLICY(SC_NOT_FOUND, "NoSuchBucketPolicy", "Bucket does not have a bucket policy"),
    OPERATION_ABORTED(SC_CONFLICT, "OperationAborted", "A conflicting conditional operation is currently in progress "
            + "against this resource. Try again"),
    PERMANENT_REDIRECT(SC_MOVED_PERMANENTLY, "PermanentRedirect", "Access to the bucket permanently redirected to this endpoint"),
    PRECONDITION_FAILED(SC_PRECONDITION_FAILED, "PreconditionFailed", "One of the preconditions specified did not hold"),
    REDIRECT(SC_TEMPORARY_REDIRECT, "Redirect", "Temporary redirect"),
    RESTORE_ALREADY_IN_PROGRESS(SC_CONFLICT, "RestoreAlreadyInProgress", "Object restore is already in progress"),
    REQUEST_IS_NOT_MULTI_PART_CONTENT(SC_BAD_REQUEST, "RequestIsNotMultiPartContent",
            "Bucket POST must be of the enclosure-type multipart/form-data"),
    REQUEST_TIMEOUT(SC_BAD_REQUEST, "RequestTimeout", "request timed out"),
    REQUEST_TIME_TOO_SKEWED(SC_FORBIDDEN, "RequestTimeTooSkewed",
            "The difference between the request time and the server's time is too large"),
    REQUEST_TORRENT_OF_BUCKET_ERROR(SC_BAD_REQUEST, "RequestTorrentOfBucketError",
            "Requesting the torrent file of a bucket is not permitted"),
    SIGNATURE_DOES_NOT_MATCH(SC_FORBIDDEN, "SignatureDoesNotMatch", "The request signature does not match"),
    SERVICE_UNAVAILABLE(SC_SERVICE_UNAVAILABLE, "ServiceUnavailable", "Service unavailable.  Retry again"),
    SLOW_DOWN(SC_SERVICE_UNAVAILABLE, "SlowDown", "Reduce request rate"),
    TEMPORARY_REDIRECT(SC_TEMPORARY_REDIRECT, "TemporaryRedirect", "Temporary redirect due to DNS updates in progress"),
    TOKEN_REFRESH_REQUIRED(SC_BAD_REQUEST, "TokenRefreshRequired", "The provided token must be refreshed"),
    TOO_MANY_BUCKETS(SC_BAD_REQUEST, "TooManyBuckets", "Bucket creation is not allowed due to maximum limit reached"),
    UNEXPECTED_CONTENT(SC_BAD_REQUEST, "UnexpectedContent", "Request does not support content"),
    UNRESOLVABLE_GRANT_BY_EMAIL_ADDRESS(SC_BAD_REQUEST, "UnresolvableGrantByEmailAddress", "The email address provided does not match"),
    USER_KEY_MUST_BE_SPECIFIED(SC_BAD_REQUEST, "UserKeyMustBeSpecified",
            "The bucket POST must contain the specified field name or check the order of the fields"),
    X_AMZ_CONTENT_SHA256_MISMATCH(SC_BAD_REQUEST, "XAmzContentSHA256Mismatch",
            "content SHA256 mismatch");
    private int statusCode;
    private String name;
    private String description;

    ErrorCode(int statusCode, String name, String description) {
        this.statusCode = statusCode;
        this.name = name;
        this.description = description;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
