package us.cuatoi.s34jserver.core.handler.bucket;

import org.apache.commons.lang3.StringUtils;
import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;
import static us.cuatoi.s34jserver.core.S3Constants.POLICY_JSON;
import static us.cuatoi.s34jserver.core.helper.PathHelper.md5HashFile;

public class BucketHandler extends BaseHandler {

    protected final Path bucketDir;
    protected final String bucketName;
    protected final Path bucketMetadataDir;
    protected final Path bucketPolicyFile;
    protected final Path bucketUploadDir;

    protected BucketHandler(StorageContext context, Request request) {
        super(context, request);
        bucketName = request.getBucketName();
        bucketDir = baseDir.resolve(bucketName);
        bucketMetadataDir = baseMetadataDir.resolve(bucketName);
        bucketPolicyFile = bucketMetadataDir.resolve(POLICY_JSON);
        bucketUploadDir = baseUploadDir.resolve(bucketName);
    }

    @Override
    public Response handle() throws Exception {
        switch (lowerCase(request.getMethod())) {
            case "head":
                return handleHead();
            case "put":
                return handlePut();
            case "delete":
                return handleDelete();
            default:
                throw new S3Exception(ErrorCode.NOT_IMPLEMENTED);
        }
    }

    private Response handleDelete() throws IOException {
        verifyBucketExists();
        PathHelper.deleteDir(bucketUploadDir);
        PathHelper.deleteDir(bucketMetadataDir);
        PathHelper.deleteDir(bucketDir);
        return new Response();
    }

    private Response handlePut() throws IOException {
        if (Files.exists(bucketDir)) {
            throw new S3Exception(ErrorCode.BUCKET_ALREADY_EXISTS);
        }
        Files.createDirectories(bucketDir);
        logger.info("Created " + bucketDir);
        Files.createDirectories(bucketMetadataDir);
        logger.info("Created " + bucketMetadataDir);
        Files.createDirectories(bucketUploadDir);
        logger.info("Created " + bucketUploadDir);
        return new Response();
    }


    private Response handleV2() {
        return null;
    }

    private Response handleHead() {
        verifyBucketExists();
        return new Response().setStatus(200);
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


    public static class Builder extends BaseHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && !equalsIgnoreCase(request.getMethod(), "get");
            ok = ok && !StringUtils.containsAny(request.getQueryString(), "location", "uploads", "policy");
            return ok;
        }

        @Override
        public BaseHandler create(SimpleStorageContext context, Request request) {
            return new BucketHandler(context, request);
        }
    }
}
