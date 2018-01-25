package us.cuatoi.s34jserver.core.handler.object;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.BaseEncoding;
import org.apache.commons.io.IOUtils;
import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.dto.*;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.handler.bucket.BucketHandler;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.S3Constants.*;
import static us.cuatoi.s34jserver.core.helper.LogHelper.debugMultiline;
import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;

public class MultipartUploadHandler extends ObjectHandler {

    protected final String uploadId;
    protected final Path uploadDir;
    protected final Path uploadMetadataFile;

    protected MultipartUploadHandler(StorageContext context, Request request) {
        super(context, request);
        uploadId = getUploadId(request);
        uploadDir = objectUploadDir.resolve(uploadId);
        uploadMetadataFile = uploadDir.resolve(S3Constants.METADATA_JSON);
    }


    private String getUploadId(Request request) {
        String id = request.getQueryParameter("uploadId");
        return isNotBlank(id) ? id : generateNewId();
    }

    @VisibleForTesting
    public static String generateNewId() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[24]);
        buffer.putLong(System.currentTimeMillis());
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return BaseEncoding.base32Hex().encode(buffer.array()).replace("=", "");
    }

    @Override
    public Response handle() throws Exception {
        switch (lowerCase(request.getMethod())) {
            case "get":
                return listPart();
            case "post":
                return isBlank(request.getHeader("uploadId")) ?
                        initiateMultipartUpload() :
                        completeMultipartUpload();
            case "put":
                return uploadPart();
            case "delete":
                return abortUpload();
            default:
                throw new S3Exception(ErrorCode.NOT_IMPLEMENTED);
        }
    }

    private Response completeMultipartUpload() throws Exception {
        CompleteMultipartUploadXml dto = DTOHelper.parseXmlContent(request.getContent(), new CompleteMultipartUploadXml());
        verifyUploadExists();
        verifyParts(dto);

        Files.createDirectories(objectFile.getParent());
        try (OutputStream os = Files.newOutputStream(objectFile)) {
            for (PartXml part : dto.getParts()) {
                try (InputStream is = Files.newInputStream(uploadDir.resolve(part.getPartNumber()))) {
                    IOUtils.copy(is, os);
                }
            }
        }
        logger.info("Created " + objectFile);

        String eTag = calculateETag();
        ObjectMetadata metadata = DTOHelper.fromJson(uploadMetadataFile, ObjectMetadata.class);
        metadata.seteTag(eTag);
        saveMetadata(metadata);

        PathHelper.deleteDir(uploadDir);

        CompleteMultipartUploadResultDTO content = new CompleteMultipartUploadResultDTO();
        content.setBucket(bucketName);
        content.setKey(objectName);
        content.seteTag(eTag);
        content.setLocation(request.getUrl());
        return new Response()
                .setContent(content).setHeader("ETag", eTag);
    }

    private void verifyParts(CompleteMultipartUploadXml dto) throws IOException {
        PartXml lastPart = null;
        for (PartXml part : dto.getParts()) {
            Path partFile = uploadDir.resolve(part.getPartNumber());
            if (!Files.exists(partFile)) {
                throw new S3Exception(ErrorCode.INVALID_PART);
            }
            if (!equalsIgnoreCase(part.geteTag(), getOrCalculateETag(partFile))) {
                throw new S3Exception(ErrorCode.INVALID_PART);
            }
            if (lastPart != null && Integer.parseInt(part.getPartNumber()) <= Integer.parseInt(lastPart.getPartNumber())) {
                throw new S3Exception(ErrorCode.INVALID_PART_ORDER);
            }
            lastPart = part;
        }
    }

    private Response listPart() throws Exception {
        String encodingType = request.getQueryParameter("encoding-type");
        long maxParts = parseLong(request.getQueryParameter("max-parts"), 1000);
        String partNumberMarker = request.getQueryParameter("part-number-marker");
        verifyUploadExists();
        logger.info("encodingType=" + encodingType);
        logger.info("maxParts=" + maxParts);
        logger.info("partNumberMarker=" + partNumberMarker);

        InitiatorXml id = new InitiatorXml();
        id.setDisplayName(context.getServerId());
        id.setId(context.getServerId());

        OwnerXml od = new OwnerXml();
        od.setDisplayName(context.getServerId());
        od.setId(context.getServerId());

        ListPartsResultXml dto = new ListPartsResultXml();
        dto.setMaxParts(maxParts);
        dto.setBucket(bucketName);
        dto.setEncodingType(encodingType);
        dto.setInitiator(id);
        dto.setOwner(od);
        dto.setPartNumberMarker(partNumberMarker);
        dto.setStorageClass(STORAGE_CLASS);
        Files.list(uploadDir).sorted(Comparator.naturalOrder())
                .forEach((part) -> {
                    String partNumber = part.getFileName().toString();
                    if (equalsIgnoreCase(partNumber, METADATA_JSON)) {
                        logger.trace("Skipped metadata " + partNumber);
                        return;
                    }
                    if (endsWith(partNumber, S3Constants.ETAG_SUFFIX)) {
                        logger.trace("Skipped etag " + partNumber);
                        return;
                    }
                    if (isNotBlank(partNumberMarker) && compare(partNumber, partNumber) <= 0) {
                        logger.trace("Skipped due to partNumberMarker" + partNumber);
                        return;
                    }
                    if (dto.getParts().size() < maxParts) {
                        PartResponseXml prd = new PartResponseXml();
                        prd.setPartNumber(Long.parseLong(partNumber));
                        prd.setLastModified(PathHelper.getLastModifiedStringUnchecked(part));
                        prd.seteTag(getOrCalculateETagUnchecked(part));
                        prd.setSize(PathHelper.sizeUnchecked(part));
                        dto.getParts().add(prd);
                        dto.setNextPartNumberMarker(partNumber);
                    } else {
                        logger.trace("Updated truncate = true due to part " + partNumber);
                        dto.setTruncated(true);
                    }
                });
        if (!dto.isTruncated()) {
            dto.setNextPartNumberMarker(null);
        }
        return new Response().setContent(dto);
    }

    protected String getOrCalculateETag(Path partFile) throws IOException {
        Path eTagFile = uploadDir.resolve(partFile.getFileName().toString() + ETAG_SUFFIX);
        String savedETag;
        if (Files.exists(eTagFile)) {
            savedETag = new String(Files.readAllBytes(eTagFile), UTF_8);
        } else {
            savedETag = calculateETag(partFile);
            Files.write(eTagFile, savedETag.getBytes(UTF_8));
        }
        return savedETag;
    }

    protected String getOrCalculateETagUnchecked(Path partFile) {
        try {
            return getOrCalculateETag(partFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Response abortUpload() throws IOException {
        verifyUploadExists();
        PathHelper.deleteDir(uploadDir);
        return new Response();
    }

    private Response uploadPart() throws Exception {
        verifyUploadExists();
        String partNumber = request.getQueryParameter("partNumber");
        verifyPartNumber(partNumber);
        Path partFile = uploadDir.resolve(partNumber);
        Files.copy(request.getContent(), partFile, StandardCopyOption.REPLACE_EXISTING);

        Path eTagFile = uploadDir.resolve(partNumber + ETAG_SUFFIX);
        String ETag = calculateETag(request.getContent());
        Files.write(eTagFile, ETag.getBytes(StandardCharsets.UTF_8));

        logger.info("Created " + partFile);
        return new Response().setHeader("ETag", ETag);
    }

    protected void verifyUploadExists() {
        if (!Files.exists(uploadDir)) {
            throw new S3Exception(ErrorCode.NO_SUCH_UPLOAD);
        }
    }

    private void verifyPartNumber(String partNumber) {
        try {
            Integer.parseInt(partNumber);
        } catch (NumberFormatException ex) {
            throw new S3Exception(ErrorCode.INTERNAL_ERROR);
        }
    }

    private Response initiateMultipartUpload() throws Exception {
        Files.createDirectories(uploadDir);
        logger.info("Created " + uploadDir);

        ObjectMetadata metadata = createMetadata(null);
        String metadataString = DTOHelper.toPrettyJson(metadata);
        Files.write(uploadMetadataFile, metadataString.getBytes("UTF-8"));
        logger.info("Updated " + uploadMetadataFile);
        debugMultiline(logger, "Metadata: " + metadataString);

        InitiateMultipartUploadResultXml content = new InitiateMultipartUploadResultXml();
        content.setBucket(bucketName);
        content.setKey(objectName);
        content.setUploadId(uploadId);
        return new Response().setContent(content);
    }

    public static class Builder extends BucketHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isNotBlank(request.getObjectName());
            ok = ok && containsAny(request.getQueryString(), "uploads", "uploadId");
            ok = ok && request.getFormParameter("fileName") == null;
            return ok;
        }

        @Override
        public BaseHandler create(SimpleStorageContext context, Request request) {
            return new ObjectHandler(context, request);
        }
    }
}
