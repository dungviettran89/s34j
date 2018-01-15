package us.cuatoi.s34jserver.core.operation.object;

import com.google.common.collect.Lists;
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
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiConsumer;

import static java.nio.file.Files.getLastModifiedTime;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.helper.PathHelper.md5HashFile;

public abstract class ObjectS3RequestHandler<F extends ObjectS3Request, T extends S3Response>
        extends BucketS3RequestHandler<F, T> {

    public static final CharSequence[] STORED_HEADERS = {"Cache-Control", "Content-Type", "Content-Disposition", "Content-Encoding", "Expires"};
    protected final Path objectFile;
    protected final Path objectMetadataFile;
    protected final Path objectUploadDir;
    protected final Path objectMetadataDir;
    protected final String objectName;

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
        return md5HashFile(file);
    }

    protected void verifyObjectExists() {
        if (!Files.exists(objectFile)) {
            throw new S3Exception(ErrorCode.NO_SUCH_KEY);
        }
    }

    protected ObjectMetadata createMetadata(String eTag) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata()
                .seteTag(eTag);
        BiConsumer<String, String> metadataHandler = (k, v) -> {
            String name = lowerCase(k);
            if (equalsAnyIgnoreCase(name, STORED_HEADERS)) {
                metadata.setHeader(name, v);
            }
            if (startsWith(name, "x-amz-meta-")) {
                metadata.setMetadata(name, v);
            }
            if (equalsIgnoreCase(name, "x-amz-website-redirect-location")) {
                metadata.setRedirectLocation(v);
            }
        };
        s3Request.getHeaders().forEach(metadataHandler);
        s3Request.getFormParameters().forEach(metadataHandler);
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
        logger.debug("Updated " + objectMetadataFile);
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
        s3Request.getQueryParameters().forEach((k, v) -> {
            if (startsWith(k, "response-")) {
                String header = remove(k, "response-");
                response.setHeader(header, v);
            }
        });
        response.setHeader("Last-Modified", S3Constants.HTTP_HEADER_DATE_FORMAT.print(getLastModifiedTime(objectFile).toMillis()));
        response.setHeader("Content-Length", String.valueOf(Files.size(objectFile)));
        return response;
    }

    protected String saveObjectAndMetadata() throws IOException {
        Files.createDirectories(objectFile.getParent());
        Files.copy(s3Request.getContent(), objectFile, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Saved " + objectFile);
        Files.createDirectories(objectUploadDir);
        logger.info("Created " + objectUploadDir);

        String eTag = calculateETag(s3Request.getContent());
        ObjectMetadata metadata = createMetadata(eTag);
        saveMetadata(metadata);
        return eTag;
    }
}
