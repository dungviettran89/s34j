package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class DeleteMultipleObjectsS3Request extends BucketS3Request {
    public DeleteMultipleObjectsS3Request() {
    }

    public DeleteMultipleObjectsS3Request(S3Request request) {
        super(request);
    }
}
