package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class ListObjectsV1S3Request extends BucketS3Request {
    public ListObjectsV1S3Request() {
    }

    public ListObjectsV1S3Request(S3Request request) {
        super(request);
    }
}
