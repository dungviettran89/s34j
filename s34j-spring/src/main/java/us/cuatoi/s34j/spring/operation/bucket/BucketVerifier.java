package us.cuatoi.s34j.spring.operation.bucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;

public class BucketVerifier {

    public static final Logger logger = LoggerFactory.getLogger(BucketVerifier.class);

    public static boolean verifyBucketName(String name) {
        if (name == null) {
            throw new SpringStorageException(ErrorCode.INVALID_BUCKET_NAME);
        }

        // Bucket names cannot be no less than 3 and no more than 63 characters long.
        if (name.length() < 3 || name.length() > 63) {
            String message = "bucket name must be at least 3 and no more than 63 characters long";
            throw new SpringStorageException(ErrorCode.INVALID_BUCKET_NAME, message);
        }
        // Successive periods in bucket names are not allowed.
        if (name.matches("\\.\\.")) {
            String message = "bucket name cannot contain successive periods. For more information refer "
                    + "http://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html";
            throw new SpringStorageException(ErrorCode.INVALID_BUCKET_NAME, message);
        }
        // Bucket names should be dns compatible.
        if (!name.matches("^[a-z0-9][a-z0-9\\.\\-]+[a-z0-9]$")) {
            String message = "bucket name does not follow Amazon S3 standards. For more information refer "
                    + "http://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html";
            throw new SpringStorageException(ErrorCode.INVALID_BUCKET_NAME, message);
        }
        return true;
    }
}
