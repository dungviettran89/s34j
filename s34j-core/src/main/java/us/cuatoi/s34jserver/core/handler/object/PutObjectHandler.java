package us.cuatoi.s34jserver.core.handler.object;

import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.dto.CopyObjectResultXml;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.handler.bucket.BucketHandler;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.ErrorCode.PRECONDITION_FAILED;
import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;

public class PutObjectHandler extends ObjectHandler {

    private String copySource;
    private String metadataDirective;
    private String copyIfMatch;
    private String copyIfNonMatch;
    private String copyIfUnmodifiedSince;
    private String copyIfModifiedSince;
    private String taggingDirective;
    private String storageClass;
    private String websiteRedirectLocation;

    protected PutObjectHandler(StorageContext context, Request request) {
        super(context, request);
        copySource = request.getHeader("x-amz-copy-source");
        metadataDirective = request.getHeader("x-amz-metadata-directive");
        copyIfMatch = request.getHeader("x-amz-copy-source-if-match");
        copyIfNonMatch = request.getHeader("x-amz-copy-source-if-none-match");
        copyIfUnmodifiedSince = request.getHeader("x-amz-copy-source-if-unmodified-since");
        copyIfModifiedSince = request.getHeader("x-amz-copy-source-if-modified-since");
        taggingDirective = request.getHeader("x-amz-tagging-directive");
        storageClass = request.getHeader("x-amz-storage-class");
        websiteRedirectLocation = request.getHeader("x-amz-website-redirect-location");
    }

    @Override
    public Response handle() throws Exception {
        if (isNotBlank(copySource)) {
            return copyObject();
        } else {
            return saveNewObject();
        }
    }

    private Response saveNewObject() throws Exception {
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
            metadata.setRedirectLocation(request.getHeader("x-amz-website-redirect-location"));
            logger.info("Copied metadata from:" + sourceMetadata);
            logger.info("                  to:" + objectMetadataFile);
            saveMetadata(metadata);
        }
        CopyObjectResultXml dto = new CopyObjectResultXml();
        dto.seteTag(sourceETag);
        dto.setLastModified(S3Constants.EXPIRATION_DATE_FORMAT.print(modifiedTime));
        return new Response().setContent(dto);
    }

    private Response copyObject() throws Exception {
        String eTag = saveObjectAndMetadata();
        return new Response().setHeader("ETag", eTag);
    }

    public static class Builder extends BucketHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isNotBlank(request.getObjectName());
            ok = ok && equalsIgnoreCase(request.getMethod(), "put");
            ok = ok && !containsAny(request.getQueryString(), "uploads", "uploadId");
            ok = ok && request.getFormParameter("fileName") == null;
            return ok;
        }

        @Override
        public BaseHandler create(SimpleStorageContext context, Request request) {
            return new PutObjectHandler(context, request);
        }
    }
}
