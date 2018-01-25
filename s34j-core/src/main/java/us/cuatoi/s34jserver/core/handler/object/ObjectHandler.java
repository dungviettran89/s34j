package us.cuatoi.s34jserver.core.handler.object;

import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.handler.bucket.BucketHandler;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.helper.LogHelper;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.BiConsumer;

import static java.nio.file.Files.getLastModifiedTime;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.helper.PathHelper.md5HashFile;

public class ObjectHandler extends BucketHandler {

    public static final CharSequence[] STORED_HEADERS = {"Cache-Control", "Content-Type", "Content-Disposition", "Content-Encoding", "Expires"};
    protected final Path objectFile;
    protected final Path objectMetadataFile;
    protected final Path objectUploadDir;
    protected final Path objectMetadataDir;
    protected final String objectName;

    protected ObjectHandler(StorageContext context, Request request) {
        super(context, request);
        objectName = request.getObjectName();
        objectFile = bucketDir.resolve(objectName);
        objectMetadataDir = bucketMetadataDir.resolve(request.getObjectName());
        objectMetadataFile = objectMetadataDir.resolve(S3Constants.METADATA_JSON);
        objectUploadDir = bucketUploadDir.resolve(request.getObjectName());
    }

    @Override
    public Response handle() throws Exception {
        switch (lowerCase(request.getMethod())) {
            case "head":
                return handleObjectHead();
            case "get":
                return handleObjectGet();
            case "delete":
                return handleObjectDelete();
            default:
                throw new S3Exception(ErrorCode.NOT_IMPLEMENTED);
        }
    }

    private Response handleObjectDelete() throws Exception {
        verifyObjectExists();
        PathHelper.deleteDir(objectMetadataDir);
        PathHelper.deleteDir(objectUploadDir);
        Files.delete(objectFile);
        logger.info("Deleted " + objectFile);
        return new Response().setStatus(HttpServletResponse.SC_NO_CONTENT);
    }


    private Response handleObjectGet() throws IOException {
        return buildGetResponse().setContent(objectFile);
    }

    private Response handleObjectHead() throws IOException {
        return buildGetResponse();
    }

    private Response buildGetResponse() throws IOException {
        verifyObjectExists();
        Response response = new Response();
        if (Files.exists(objectMetadataFile)) {
            ObjectMetadata metadata = DTOHelper.fromJson(objectMetadataFile, ObjectMetadata.class);
            metadata.getHeaders().forEach(response::setHeader);
            metadata.getMetadata().forEach(response::setHeader);
        }
        request.getQueryParameters().forEach((k, v) -> {
            if (startsWith(k, "response-")) {
                String header = remove(k, "response-");
                response.setHeader(header, v);
            }
        });
        response.setHeader("Last-Modified", S3Constants.HTTP_HEADER_DATE_FORMAT.print(getLastModifiedTime(objectFile).toMillis()));
        response.setHeader("Content-Length", String.valueOf(Files.size(objectFile)));
        return response;
    }

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
        request.getHeaders().forEach(metadataHandler);
        request.getFormParameters().forEach(metadataHandler);
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

    protected String saveObjectAndMetadata() throws IOException {
        Files.createDirectories(objectFile.getParent());
        Files.copy(request.getContent(), objectFile, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Saved " + objectFile);
        Files.createDirectories(objectUploadDir);
        logger.info("Created " + objectUploadDir);

        String eTag = calculateETag(request.getContent());
        ObjectMetadata metadata = createMetadata(eTag);
        saveMetadata(metadata);
        return eTag;
    }


    public static class Builder extends BucketHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isNotBlank(request.getObjectName());
            ok = ok && !equalsAnyIgnoreCase(request.getMethod(), "put");
            ok = ok && !containsAny(request.getQueryString(), "uploads", "uploadId");
            ok = ok && request.getFormParameter("fileName") == null;
            return ok;
        }

        @Override
        public BaseHandler create(SimpleStorageContext context, Request request) {
            return new ObjectHandler(context, request);
        }
    }
}
