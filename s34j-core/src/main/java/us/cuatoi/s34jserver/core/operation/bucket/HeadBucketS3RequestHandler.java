package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.model.S3Response;
import us.cuatoi.s34jserver.core.model.bucket.HeadBucketS3Request;

import java.io.IOException;

public class HeadBucketS3RequestHandler extends BucketS3RequestHandler<HeadBucketS3Request,S3Response> {
    public HeadBucketS3RequestHandler(S3Context context, HeadBucketS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public S3Response handle() throws IOException {
        verifyBucketExists();
        return new S3Response(s3Request);
    }
}
