package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class PutBucketS3Request extends BucketS3Request {
    public PutBucketS3Request() {
    }

    public PutBucketS3Request(S3Request request) {
        super(request);
    }
}
