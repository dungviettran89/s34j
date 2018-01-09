package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class DeleteBucketS3Request extends BucketS3Request {
    public DeleteBucketS3Request() {
    }

    public DeleteBucketS3Request(S3Request request) {
        super(request);
    }
}
