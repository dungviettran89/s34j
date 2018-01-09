package us.cuatoi.s34jserver.core.operation.object;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.model.object.GetObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.GetObjectS3Response;

import java.io.IOException;

public class GetObjectS3RequestHandler extends ObjectS3RequestHandler<GetObjectS3Request, GetObjectS3Response> {
    public GetObjectS3RequestHandler(S3Context context, GetObjectS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    protected GetObjectS3Response handleObject() throws IOException {
        verifyObjectExists();
        return (GetObjectS3Response) new GetObjectS3Response(s3Request).setContent(objectFile);
    }

}
