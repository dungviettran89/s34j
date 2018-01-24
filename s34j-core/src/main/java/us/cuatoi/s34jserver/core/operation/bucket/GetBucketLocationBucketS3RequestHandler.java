package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.LocationConstraintXml;
import us.cuatoi.s34jserver.core.model.bucket.GetBucketLocationBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.GetBucketLocationBucketS3Response;

import java.io.IOException;

public class GetBucketLocationBucketS3RequestHandler extends BucketS3RequestHandler<GetBucketLocationBucketS3Request, GetBucketLocationBucketS3Response> {
    public GetBucketLocationBucketS3RequestHandler(S3Context context, GetBucketLocationBucketS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public GetBucketLocationBucketS3Response handle() throws IOException {
        verifyBucketExists();
        LocationConstraintXml response = new LocationConstraintXml();
        response.setRegion(context.getRegion());
        return (GetBucketLocationBucketS3Response) new GetBucketLocationBucketS3Response(s3Request).setContent(response);
    }

}
