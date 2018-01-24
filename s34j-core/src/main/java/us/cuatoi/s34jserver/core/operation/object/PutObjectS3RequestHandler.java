package us.cuatoi.s34jserver.core.operation.object;

import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.dto.CopyObjectResultXml;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.model.object.PutObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.PutObjectS3Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.ErrorCode.PRECONDITION_FAILED;
import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;

public class PutObjectS3RequestHandler extends ObjectS3RequestHandler<PutObjectS3Request, PutObjectS3Response> {

    private String copySource;
    private String metadataDirective;
    private String copyIfMatch;
    private String copyIfNonMatch;
    private String copyIfUnmodifiedSince;
    private String copyIfModifiedSince;
    private String taggingDirective;
    private String storageClass;
    private String websiteRedirectLocation;

    public PutObjectS3RequestHandler(S3Context context, PutObjectS3Request s3Request) {
        super(context, s3Request);
        copySource = s3Request.getHeader("x-amz-copy-source");
        metadataDirective = s3Request.getHeader("x-amz-metadata-directive");
        copyIfMatch = s3Request.getHeader("x-amz-copy-source-if-match");
        copyIfNonMatch = s3Request.getHeader("x-amz-copy-source-if-none-match");
        copyIfUnmodifiedSince = s3Request.getHeader("x-amz-copy-source-if-unmodified-since");
        copyIfModifiedSince = s3Request.getHeader("x-amz-copy-source-if-modified-since");
        taggingDirective = s3Request.getHeader("x-amz-tagging-directive");
        storageClass = s3Request.getHeader("x-amz-storage-class");
        websiteRedirectLocation = s3Request.getHeader("x-amz-website-redirect-location");
    }

    @Override
    protected PutObjectS3Response handleObject() throws IOException {
        if (isNotBlank(copySource)) {
            return copyObject();
        } else {
            return saveNewObject();
        }
    }

    private PutObjectS3Response copyObject() throws IOException {
        Path sourceObject = baseDir.resolve(copySource);
        Path sourceMetadata = baseMetadataDir.resolve(copySource).resolve(METADATA_JSON);

        if (!Files.exists(sourceObject) || !Files.isRegularFile(sourceObject)) {
            logger.info("NO_SUCH_OBJECT sourceObject=" + sourceObject);
            throw new S3Exception(ErrorCode.NO_SUCH_OBJECT);
        }
        if (isNotBlank(metadataDirective) && !equalsAny(metadataDirective, "COPY", "REPLACE")) {
            logger.info("INVALID_ARGUMENT metadataDirective=" + metadataDirective);
            throw new S3Exception(ErrorCode.INVALID_ARGUMENT);
        }
        if (isNotBlank(taggingDirective) && !equalsAny(taggingDirective, "COPY", "REPLACE")) {
            logger.info("INVALID_ARGUMENT taggingDirective=" + taggingDirective);
            throw new S3Exception(ErrorCode.INVALID_ARGUMENT);
        }
        String sourceETag = getObjectETag(sourceObject);
        FileTime lastModifiedTime = Files.getLastModifiedTime(sourceObject);
        long modifiedTime = lastModifiedTime.toMillis();
        if (isNotBlank(copyIfMatch) && !equalsIgnoreCase(sourceETag, copyIfMatch)) {
            logger.info("PRECONDITION_FAILED copyIfMatch:" + copyIfMatch + "!=" + sourceETag);
            throw new S3Exception(PRECONDITION_FAILED);
        }
        if (isNotBlank(copyIfNonMatch) && equalsIgnoreCase(sourceETag, copyIfMatch)) {
            logger.info("PRECONDITION_FAILED copyIfNonMatch:" + copyIfNonMatch + "==" + sourceETag);
            throw new S3Exception(PRECONDITION_FAILED);
        }
        if (isNotBlank(copyIfUnmodifiedSince)
                && S3Constants.HTTP_HEADER_DATE_FORMAT.parseDateTime(copyIfUnmodifiedSince).isBefore(modifiedTime)) {
            if (!equalsIgnoreCase(sourceETag, copyIfMatch)) {
                logger.info("PRECONDITION_FAILED copyIfUnmodifiedSince:" + copyIfUnmodifiedSince + " < " + lastModifiedTime);
                throw new S3Exception(PRECONDITION_FAILED);
            }
        }
        if (isNotBlank(copyIfModifiedSince) &&
                S3Constants.HTTP_HEADER_DATE_FORMAT.parseDateTime(copyIfModifiedSince).isAfter(modifiedTime)) {
            logger.info("PRECONDITION_FAILED copyIfModifiedSince:" + copyIfModifiedSince + " > " + lastModifiedTime);
            throw new S3Exception(PRECONDITION_FAILED);

        }

        //perform copy object
        Files.copy(sourceObject, objectFile, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Copied from:" + sourceObject);
        logger.info("         to:" + objectFile);
        Files.createDirectories(objectUploadDir);
        logger.info("Created " + objectUploadDir);

        //perform copy metadata if needed
        if ("REPLACE".equalsIgnoreCase(metadataDirective)) {
            ObjectMetadata metadata = createMetadata(sourceETag);
            saveMetadata(metadata);
        } else {
            ObjectMetadata metadata = DTOHelper.fromJson(sourceMetadata, ObjectMetadata.class);
            metadata.setRedirectLocation(s3Request.getHeader("x-amz-website-redirect-location"));
            logger.info("Copied metadata from:" + sourceMetadata);
            logger.info("                  to:" + objectMetadataFile);
            saveMetadata(metadata);
        }
        CopyObjectResultXml dto = new CopyObjectResultXml();
        dto.seteTag(sourceETag);
        dto.setLastModified(S3Constants.EXPIRATION_DATE_FORMAT.print(modifiedTime));
        return (PutObjectS3Response) new PutObjectS3Response(s3Request).setContent(dto);
    }

    private PutObjectS3Response saveNewObject() throws IOException {
        String eTag = saveObjectAndMetadata();
        return (PutObjectS3Response) new PutObjectS3Response(s3Request).setHeader("ETag", eTag);
    }

}
