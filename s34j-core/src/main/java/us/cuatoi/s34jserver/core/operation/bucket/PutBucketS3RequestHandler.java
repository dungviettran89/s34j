package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.model.bucket.PutBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.PutBucketS3Response;

import java.io.IOException;
import java.nio.file.Files;

public class PutBucketS3RequestHandler extends BucketS3RequestHandler<PutBucketS3Request, PutBucketS3Response> {


    public PutBucketS3RequestHandler(S3Context context, PutBucketS3Request s3Request) {
        super(context, s3Request);
    }

    public PutBucketS3Response handle() throws IOException {
        if (Files.exists(bucketDir)) {
            throw new S3Exception(ErrorCode.BUCKET_ALREADY_EXISTS);
        }
        Files.createDirectories(bucketDir);
        logger.info("Created " + bucketDir);
        Files.createDirectories(bucketMetadataDir);
        logger.info("Created " + bucketMetadataDir);
        Files.createDirectories(bucketUploadDir);
        logger.info("Created " + bucketUploadDir);
        return (PutBucketS3Response) new PutBucketS3Response(s3Request).setStatusCode(200);
    }
}
