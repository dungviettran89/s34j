package us.cuatoi.s34jserver.core.operation.object;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.helper.LogHelper;
import us.cuatoi.s34jserver.core.model.S3Response;
import us.cuatoi.s34jserver.core.model.object.GetObjectS3Response;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.model.object.ObjectS3Request;
import us.cuatoi.s34jserver.core.operation.bucket.BucketS3RequestHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWith;

public abstract class ObjectS3RequestHandler<F extends ObjectS3Request, T extends S3Response>
        extends BucketS3RequestHandler<F, T> {

    protected final Path objectFile;
    protected final Path objectMetadataFile;
    protected final Path objectUploadDir;
    protected final Path objectMetadataDir;
    protected final String objectName;
    protected final List<String> storedHeaders = Lists.newArrayList("cache-control",
            "content-disposition",
            "content-encoding",
            "content-type",
            "expires");

    public ObjectS3RequestHandler(S3Context context, F s3Request) {
        super(context, s3Request);
        objectName = s3Request.getObjectName();
        objectFile = bucketDir.resolve(objectName);
        objectMetadataDir = bucketMetadataDir.resolve(s3Request.getObjectName());
        objectMetadataFile = objectMetadataDir.resolve(S3Constants.METADATA_JSON);
        objectUploadDir = bucketUploadDir.resolve(s3Request.getObjectName());
    }

    @Override
    public T handle() throws IOException {
        verifyBucketExists();
        return handleObject();
    }

    protected abstract T handleObject() throws IOException;

    protected String calculateETag() throws IOException {
        return calculateETag(objectFile);
    }

    @SuppressWarnings("deprecation")
    protected String calculateETag(Path file) throws IOException {
        return com.google.common.io.Files.asByteSource(file.toFile()).hash(Hashing.md5()).toString();
    }

    protected void verifyObjectExists() {
        if (!Files.exists(objectFile)) {
            throw new S3Exception(ErrorCode.NO_SUCH_KEY);
        }
    }

    protected ObjectMetadata createMetadata(String eTag) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata()
                .seteTag(eTag);
        s3Request.getHeaders().forEach((k, v) -> {
            if (storedHeaders.contains(k)) {
                metadata.setHeader(k, v);
            }
            if (startsWith(k, "x-amz-meta-")) {
                metadata.setMetadata(k, v);
            }
            if (equalsIgnoreCase(k, "x-amz-website-redirect-location")) {
                metadata.setRedirectLocation(v);
            }
        });
        return metadata;
    }

    protected void saveMetadata(ObjectMetadata metadata) throws IOException {
        Path objectMetadataDir = objectMetadataFile.getParent();
        if (!Files.exists(objectMetadataDir)) {
            Files.createDirectories(objectMetadataDir);
            logger.info("Created " + objectMetadataDir);
        }
        String metadataString = DTOHelper.toPrettyJson(metadata);
        Files.write(objectMetadataFile, metadataString.getBytes("UTF-8"));
        logger.info("Updated " + objectMetadataFile);
        LogHelper.infoMultiline(logger, "Metadata=" + metadataString);
    }

    protected GetObjectS3Response buildGetObjectResponse() throws IOException {
        verifyObjectExists();
        GetObjectS3Response response = new GetObjectS3Response(s3Request);
        if (Files.exists(objectMetadataFile)) {
            ObjectMetadata metadata = DTOHelper.fromJson(objectMetadataFile, ObjectMetadata.class);
            metadata.getHeaders().forEach(response::setHeader);
            metadata.getMetadata().forEach(response::setHeader);
        }
        BasicFileAttributes attribute = Files.readAttributes(objectFile, BasicFileAttributes.class);
        response.setHeader("Last-Modified", S3Constants.HTTP_HEADER_DATE_FORMAT.print(attribute.lastModifiedTime().toMillis()));
        response.setHeader("Content-Length", String.valueOf(Files.size(objectFile)));
        return response;
    }
}
