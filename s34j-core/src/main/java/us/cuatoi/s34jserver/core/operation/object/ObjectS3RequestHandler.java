package us.cuatoi.s34jserver.core.operation.object;

import com.google.common.hash.Hashing;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.model.S3Response;
import us.cuatoi.s34jserver.core.model.object.ObjectS3Request;
import us.cuatoi.s34jserver.core.operation.bucket.BucketS3RequestHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ObjectS3RequestHandler<F extends ObjectS3Request,T extends S3Response>
        extends BucketS3RequestHandler<F,T> {

    protected final Path objectFile;
    protected final Path objectMetadataFile;
    protected final Path objectUploadDir;
    protected final Path objectMetadataDir;

    public ObjectS3RequestHandler(S3Context context, F s3Request) {
        super(context, s3Request);
        objectFile = bucketDir.resolve(s3Request.getObjectName());
        objectMetadataDir = bucketMetadataDir.resolve(s3Request.getObjectName());
        objectMetadataFile = objectMetadataDir.resolve("metadata.json");
        objectUploadDir = bucketUploadDir.resolve(s3Request.getObjectName());
    }

    @Override
    public T handle() throws IOException {
        verifyBucketExists();
        return handleObject();
    }

    protected abstract T handleObject() throws IOException;

    protected String getETag() throws IOException {
        return com.google.common.io.Files.asByteSource(objectFile.toFile()).hash(Hashing.sha256()).toString();
    }

    protected void verifyObjectExists() {
        if(!Files.exists(objectFile)){
            throw new S3Exception(ErrorCode.NO_SUCH_OBJECT);
        }
    }
}
