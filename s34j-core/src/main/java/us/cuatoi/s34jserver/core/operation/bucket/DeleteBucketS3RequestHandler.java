package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.bucket.DeleteBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.DeleteBucketS3Response;

import java.io.IOException;

public class DeleteBucketS3RequestHandler extends BucketS3RequestHandler<DeleteBucketS3Request, DeleteBucketS3Response> {
    public DeleteBucketS3RequestHandler(S3Context context, DeleteBucketS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public DeleteBucketS3Response handle() throws IOException {
        verifyBucketExists();
        PathHelper.deleteDir(bucketUploadDir);
        PathHelper.deleteDir(bucketMetadataDir);
        PathHelper.deleteDir(bucketDir);
        return new DeleteBucketS3Response(s3Request);
    }

}
