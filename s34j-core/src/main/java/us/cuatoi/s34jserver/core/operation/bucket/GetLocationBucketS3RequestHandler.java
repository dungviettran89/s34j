package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.LocationConstraintDTO;
import us.cuatoi.s34jserver.core.model.bucket.GetLocationBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.GetLocationBucketS3Response;

import java.io.IOException;

public class GetLocationBucketS3RequestHandler extends BucketS3RequestHandler<GetLocationBucketS3Request, GetLocationBucketS3Response> {
    public GetLocationBucketS3RequestHandler(S3Context context, GetLocationBucketS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public GetLocationBucketS3Response handle() throws IOException {
        verifyBucketExists();
        LocationConstraintDTO response = new LocationConstraintDTO();
        response.setRegion(context.getRegion());
        return (GetLocationBucketS3Response) new GetLocationBucketS3Response(s3Request).setContent(response);
    }

}
