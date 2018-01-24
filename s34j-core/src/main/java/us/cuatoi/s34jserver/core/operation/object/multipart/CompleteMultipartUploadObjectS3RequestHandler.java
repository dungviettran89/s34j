package us.cuatoi.s34jserver.core.operation.object.multipart;

import org.apache.commons.io.IOUtils;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.dto.CompleteMultipartUploadResultDTO;
import us.cuatoi.s34jserver.core.dto.PartXml;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.model.object.multipart.CompleteMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.CompleteMultipartUploadObjectS3Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class CompleteMultipartUploadObjectS3RequestHandler
        extends MultipartUploadObjectS3RequestHandler<CompleteMultipartUploadObjectS3Request, CompleteMultipartUploadObjectS3Response> {
    public CompleteMultipartUploadObjectS3RequestHandler(S3Context context, CompleteMultipartUploadObjectS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    protected CompleteMultipartUploadObjectS3Response handleObject() throws IOException {
        verifyUploadExists();
        verifyParts();

        Files.createDirectories(objectFile.getParent());
        try (OutputStream os = Files.newOutputStream(objectFile)) {
            for (PartXml part : s3Request.getCompleteMultipartUploadXml().getParts()) {
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
        content.setLocation(s3Request.getUrl());
        return (CompleteMultipartUploadObjectS3Response) new CompleteMultipartUploadObjectS3Response(s3Request)
                .setContent(content).setHeader("ETag", eTag);
    }

    private void verifyParts() throws IOException {
        PartXml lastPart = null;
        for (PartXml part : s3Request.getCompleteMultipartUploadXml().getParts()) {
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

}
