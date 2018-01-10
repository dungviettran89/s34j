package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class ListMultipartUploadsBucketS3Request extends BucketS3Request {
    public ListMultipartUploadsBucketS3Request() {
    }

    public ListMultipartUploadsBucketS3Request(S3Request request) {
        super(request);
    }
}
