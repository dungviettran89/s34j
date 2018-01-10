package us.cuatoi.s34jserver.core.model.object.multipart;

import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.object.ObjectS3Request;

public class UploadPartObjectS3Request extends ObjectS3Request {
    public UploadPartObjectS3Request(S3Request request) {
        super(request);
    }
}
