package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class GetLocationBucketS3Request extends BucketS3Request {
    public GetLocationBucketS3Request() {
    }

    public GetLocationBucketS3Request(S3Request request) {
        super(request);
    }
}