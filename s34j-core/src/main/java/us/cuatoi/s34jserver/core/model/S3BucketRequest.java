package us.cuatoi.s34jserver.core.model;

public class S3BucketRequest extends S3Request {
    private String bucketName;

    /**
     * Constructors
     */

    public S3BucketRequest() {
        super();
    }

    public S3BucketRequest(S3Request request) {
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
    public S3BucketRequest setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }
}
