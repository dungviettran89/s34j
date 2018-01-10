package us.cuatoi.s34jserver.core.operation.object;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.model.object.GetObjectS3Response;
import us.cuatoi.s34jserver.core.model.object.HeadObjectS3Request;

import java.io.IOException;

public class HeadObjectS3RequestHandler extends ObjectS3RequestHandler<HeadObjectS3Request, GetObjectS3Response> {
    public HeadObjectS3RequestHandler(S3Context context, HeadObjectS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    protected GetObjectS3Response handleObject() throws IOException {
        return buildGetObjectResponse();
    }

}
