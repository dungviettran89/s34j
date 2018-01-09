package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.model.PutBucketS3Request;
import us.cuatoi.s34jserver.core.model.S3Response;

import java.io.IOException;
import java.nio.file.Files;

public class PutBucketS3RequestHandler extends BucketS3RequestHandler {

    protected final PutBucketS3Request s3Request;

    public PutBucketS3RequestHandler(S3Context context, PutBucketS3Request s3Request) {
        super(context, s3Request);
        this.s3Request = s3Request;
    }

    public S3Response handle() throws IOException {
        if (Files.exists(bucketDir)) {
            throw new S3Exception(ErrorCode.BUCKET_ALREADY_EXISTS);
        }
        Files.createDirectories(bucketDir);
        Files.createDirectories(bucketMetadataDir);
        Files.createDirectories(bucketUploadDir);
        logger.info("PUT " + s3Request.getUrl());
        logger.info("Created " + bucketDir);
        return new S3Response(s3Request).setStatusCode(200);
    }
}
