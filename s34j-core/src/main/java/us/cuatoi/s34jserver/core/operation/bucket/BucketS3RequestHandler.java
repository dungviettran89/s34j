package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.model.bucket.BucketS3Request;
import us.cuatoi.s34jserver.core.model.S3Response;
import us.cuatoi.s34jserver.core.operation.S3RequestHandler;
import us.cuatoi.s34jserver.core.operation.Verifier;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BucketS3RequestHandler<F extends BucketS3Request, T extends S3Response> extends S3RequestHandler<F, T> {

    protected final Path bucketDir;
    protected final String bucketName;
    protected final Path bucketMetadataDir;
    protected final Path bucketUploadDir;

    public BucketS3RequestHandler(S3Context context, F s3Request) {
        super(context, s3Request);
        bucketName = s3Request.getBucketName();
        bucketDir = baseDir.resolve(bucketName);
        bucketMetadataDir = baseMetadataDir.resolve(bucketName);
        bucketUploadDir = baseUploadDir.resolve(bucketName);
        Verifier.verifyBucketName(bucketName);
    }

    protected void verifyBucketExists() {
        if (!Files.exists(bucketDir)) {
            throw new S3Exception(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
