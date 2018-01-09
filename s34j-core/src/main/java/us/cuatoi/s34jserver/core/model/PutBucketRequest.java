package us.cuatoi.s34jserver.core.model;

public class PutBucketRequest extends S3BucketRequest {
    public PutBucketRequest() {
    }

    public PutBucketRequest(S3Request request) {
        super(request);
    }
}
