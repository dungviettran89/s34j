package us.cuatoi.s34jserver.core.model.object.multipart;

import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.S3Response;

public class AbortMultipartUploadObjectS3Response extends S3Response {
    public AbortMultipartUploadObjectS3Response(S3Request request) {
        super(request);
    }
}
