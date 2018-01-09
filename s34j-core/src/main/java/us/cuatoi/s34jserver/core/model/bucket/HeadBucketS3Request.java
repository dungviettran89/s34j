package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class HeadBucketS3Request extends BucketS3Request {
    public HeadBucketS3Request() {
    }

    public HeadBucketS3Request(S3Request request) {
        super(request);
    }
}
