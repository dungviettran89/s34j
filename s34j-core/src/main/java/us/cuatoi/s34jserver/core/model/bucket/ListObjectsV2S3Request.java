package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class ListObjectsV2S3Request extends BucketS3Request {
    public ListObjectsV2S3Request() {
    }

    public ListObjectsV2S3Request(S3Request request) {
        super(request);
    }
}
