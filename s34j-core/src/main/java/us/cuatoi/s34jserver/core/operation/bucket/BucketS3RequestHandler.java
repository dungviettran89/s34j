package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.model.bucket.BucketS3Request;
import us.cuatoi.s34jserver.core.model.S3Response;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.operation.S3RequestHandler;
import us.cuatoi.s34jserver.core.operation.Verifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;
import static us.cuatoi.s34jserver.core.helper.PathHelper.md5HashFile;

public abstract class BucketS3RequestHandler<F extends BucketS3Request, T extends S3Response> extends S3RequestHandler<F, T> {

    protected final Path bucketDir;
    protected final String bucketName;
    protected final Path bucketMetadataDir;
    protected final Path bucketUploadDir;

    public BucketS3RequestHandler(S3Context context, F s3Request) {
        super(context, s3Request);
        bucketName = s3Request.getBucketName();
        bucketDir = baseDir.resolve(bucketName);
        bucketMetadataDir = baseMetadataDir.resolve(bucketName);
        bucketUploadDir = baseUploadDir.resolve(bucketName);
        Verifier.verifyBucketName(bucketName);
    }

    protected void verifyBucketExists() {
        if (!Files.exists(bucketDir)) {
            throw new S3Exception(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    protected String getObjectETag(Path path) throws IOException {
        String eTag;
        Path metadataFile = bucketMetadataDir.resolve(bucketDir.relativize(path)).resolve(METADATA_JSON);
        if (Files.exists(metadataFile)) {
            ObjectMetadata metadata = DTOHelper.fromJson(metadataFile, ObjectMetadata.class);
            eTag = metadata.geteTag();
        } else {
            eTag = md5HashFile(path);
            ObjectMetadata metadata = new ObjectMetadata().seteTag(eTag);
            Files.write(metadataFile, DTOHelper.toPrettyJson(metadata).getBytes(StandardCharsets.UTF_8));
        }
        return eTag;
    }
}
