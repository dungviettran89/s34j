package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.model.BucketS3Request;
import us.cuatoi.s34jserver.core.model.S3Request;

import java.nio.file.Path;

public class BucketS3RequestHandler extends S3RequestHandler {

    protected final BucketS3Request bucketS3Request;
    protected final Path bucketDir;
    protected final String bucketName;
    protected final Path bucketMetadataDir;
    protected final Path bucketUploadDir;

    public BucketS3RequestHandler(S3Context context, S3Request s3Request) {
        super(context, s3Request);
        bucketS3Request = (BucketS3Request) s3Request;
        bucketName = bucketS3Request.getBucketName();
        bucketDir = baseDir.resolve(bucketName);
        bucketMetadataDir = baseMetadataDir.resolve(bucketName);
        bucketUploadDir = baseUploadDir.resolve(bucketName);
    }
}
