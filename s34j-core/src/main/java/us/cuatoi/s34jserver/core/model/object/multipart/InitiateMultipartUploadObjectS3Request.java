package us.cuatoi.s34jserver.core.model.object.multipart;

import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.object.ObjectS3Request;

public class InitiateMultipartUploadObjectS3Request extends ObjectS3Request {
    public InitiateMultipartUploadObjectS3Request(S3Request request) {
        super(request);
    }
}
