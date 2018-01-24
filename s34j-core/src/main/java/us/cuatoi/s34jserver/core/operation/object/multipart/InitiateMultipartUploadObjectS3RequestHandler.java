package us.cuatoi.s34jserver.core.operation.object.multipart;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.InitiateMultipartUploadResultXml;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.model.object.multipart.InitiateMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.InitiateMultipartUploadObjectS3Response;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;

import java.io.IOException;
import java.nio.file.Files;

import static us.cuatoi.s34jserver.core.helper.LogHelper.debugMultiline;

public class InitiateMultipartUploadObjectS3RequestHandler extends MultipartUploadObjectS3RequestHandler<InitiateMultipartUploadObjectS3Request, InitiateMultipartUploadObjectS3Response> {

    public InitiateMultipartUploadObjectS3RequestHandler(S3Context context, InitiateMultipartUploadObjectS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    protected InitiateMultipartUploadObjectS3Response handleObject() throws IOException {
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
        return (InitiateMultipartUploadObjectS3Response) new InitiateMultipartUploadObjectS3Response(s3Request).setContent(content);
    }

}
