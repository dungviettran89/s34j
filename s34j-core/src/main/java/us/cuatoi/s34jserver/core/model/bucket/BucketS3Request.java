package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.model.S3Request;

public class BucketS3Request extends S3Request {
    private String bucketName;

    /**
     * Constructors
     */

    public BucketS3Request() {
        super();
    }

    public BucketS3Request(S3Request request) {
        super(request);
    }

    /**
     * Getters
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Setters
     */
    public BucketS3Request setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }
}
