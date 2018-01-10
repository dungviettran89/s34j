package us.cuatoi.s34jserver.core.model.object.multipart;

import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.S3Response;

public class InitiateMultipartUploadObjectS3Response extends S3Response {
    public InitiateMultipartUploadObjectS3Response(S3Request request) {
        super(request);
    }
}
