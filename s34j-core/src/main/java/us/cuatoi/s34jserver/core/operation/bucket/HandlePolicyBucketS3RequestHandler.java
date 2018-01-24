package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.model.bucket.HandlePolicyBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.HandlePolicyBucketS3Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class HandlePolicyBucketS3RequestHandler extends BucketS3RequestHandler<HandlePolicyBucketS3Request, HandlePolicyBucketS3Response> {

    public HandlePolicyBucketS3RequestHandler(S3Context context, HandlePolicyBucketS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public HandlePolicyBucketS3Response handle() throws IOException {
        if (equalsIgnoreCase(s3Request.getMethod(), "get")) {
            return handleGet();
        } else if (equalsIgnoreCase(s3Request.getMethod(), "put")) {
            return handlePut();
        } else if (equalsIgnoreCase(s3Request.getMethod(), "delete")) {
            return handleDelete();
        }
        throw new S3Exception(ErrorCode.NOT_IMPLEMENTED);
    }

    private HandlePolicyBucketS3Response handleDelete() throws IOException {
        if (Files.deleteIfExists(bucketPolicyFile)) {
            logger.info("Deleted " + bucketPolicyFile);
        }
        return (HandlePolicyBucketS3Response) new HandlePolicyBucketS3Response(s3Request).setStatusCode(204);
    }

    private HandlePolicyBucketS3Response handlePut() throws IOException {
        Files.copy(s3Request.getContent(), bucketPolicyFile, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Updated " + bucketPolicyFile);
        return (HandlePolicyBucketS3Response) new HandlePolicyBucketS3Response(s3Request).setStatusCode(204);
    }

    private HandlePolicyBucketS3Response handleGet() throws IOException {
        Object content = "{}";
        if (Files.exists(this.bucketPolicyFile)) {
            content = this.bucketPolicyFile;
        }
        return (HandlePolicyBucketS3Response) new HandlePolicyBucketS3Response(s3Request)
                .setContentType("application/json; charset=UTF-8")
                .setContent(content);
    }
}
