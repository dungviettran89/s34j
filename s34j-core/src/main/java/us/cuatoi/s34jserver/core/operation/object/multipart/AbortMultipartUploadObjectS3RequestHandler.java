package us.cuatoi.s34jserver.core.operation.object.multipart;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.object.multipart.AbortMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.AbortMultipartUploadObjectS3Response;

import java.io.IOException;

public class AbortMultipartUploadObjectS3RequestHandler extends MultipartUploadObjectS3RequestHandler<AbortMultipartUploadObjectS3Request, AbortMultipartUploadObjectS3Response> {


    public AbortMultipartUploadObjectS3RequestHandler(S3Context context, AbortMultipartUploadObjectS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    protected AbortMultipartUploadObjectS3Response handleObject() throws IOException {
        verifyUploadExists();
        PathHelper.deleteDir(uploadDir);
        return new AbortMultipartUploadObjectS3Response(s3Request);
    }
}
