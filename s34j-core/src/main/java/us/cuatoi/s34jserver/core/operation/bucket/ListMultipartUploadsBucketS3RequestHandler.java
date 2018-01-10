package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.ListMultipartUploadsResultDTO;
import us.cuatoi.s34jserver.core.model.bucket.ListMultipartUploadsBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.ListMultipartUploadsBucketS3Response;

import java.io.IOException;

public class ListMultipartUploadsBucketS3RequestHandler extends BucketS3RequestHandler<ListMultipartUploadsBucketS3Request, ListMultipartUploadsBucketS3Response> {
    public ListMultipartUploadsBucketS3RequestHandler(S3Context context, ListMultipartUploadsBucketS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public ListMultipartUploadsBucketS3Response handle() throws IOException {
        //todo: properly implement this
        ListMultipartUploadsResultDTO result = new ListMultipartUploadsResultDTO();
        result.setBucketName(bucketName);
        return (ListMultipartUploadsBucketS3Response) new ListMultipartUploadsBucketS3Response(s3Request)
                .setContent(result);
    }
}
