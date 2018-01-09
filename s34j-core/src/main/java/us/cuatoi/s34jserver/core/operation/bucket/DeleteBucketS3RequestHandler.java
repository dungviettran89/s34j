package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.model.DeleteBucketS3Request;
import us.cuatoi.s34jserver.core.model.DeleteBucketS3Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class DeleteBucketS3RequestHandler extends BucketS3RequestHandler<DeleteBucketS3Request, DeleteBucketS3Response> {
    public DeleteBucketS3RequestHandler(S3Context context, DeleteBucketS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public DeleteBucketS3Response handle() throws IOException {
        verifyBucketExists();
        deleteDir(bucketUploadDir);
        deleteDir(bucketMetadataDir);
        deleteDir(bucketDir);
        return new DeleteBucketS3Response(s3Request);
    }

    private void deleteDir(Path dir) throws IOException {
        Files.walk(dir).sorted(Comparator.reverseOrder())
                .forEach((f) -> {
                    try {
                        Files.deleteIfExists(f);
                        logger.info("Deleted " + f);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
