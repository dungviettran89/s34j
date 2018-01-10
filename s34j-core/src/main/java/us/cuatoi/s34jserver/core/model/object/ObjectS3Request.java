package us.cuatoi.s34jserver.core.model.object;

import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.bucket.BucketS3Request;

public class ObjectS3Request extends BucketS3Request {
    private String objectName;

    /* Constructors */
    public ObjectS3Request(S3Request request) {
        super(request);
    }

    /* Getters */
    public String getObjectName() {
        return objectName;
    }

    /* Setters */
    public ObjectS3Request setObjectName(String objectName) {
        this.objectName = objectName;
        return this;
    }
}
