package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class ListObjectsS3Request extends BucketS3Request {
    public ListObjectsS3Request() {
    }

    public ListObjectsS3Request(S3Request request) {
        super(request);
    }
}
