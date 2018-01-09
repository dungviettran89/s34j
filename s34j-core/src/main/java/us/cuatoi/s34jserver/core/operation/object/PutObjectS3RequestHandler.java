package us.cuatoi.s34jserver.core.operation.object;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.helper.GsonHelper;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;
import us.cuatoi.s34jserver.core.model.object.PutObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.PutObjectS3Response;

import java.io.IOException;
import java.nio.file.Files;

public class PutObjectS3RequestHandler extends ObjectS3RequestHandler<PutObjectS3Request, PutObjectS3Response> {
    public PutObjectS3RequestHandler(S3Context context, PutObjectS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    protected PutObjectS3Response handleObject() throws IOException {
        Files.copy(s3Request.getContent(), objectFile);
        logger.info("Saved " + objectFile);
        Files.createDirectories(objectUploadDir);
        logger.info("Created " + objectUploadDir);
        Files.createDirectories(objectMetadataFile.getParent());
        logger.info("Created " + objectMetadataFile.getParent());

        String eTag = getETag();
        ObjectMetadata metadata = new ObjectMetadata()
                .seteTag(eTag)
                .setContentType(s3Request.getHeader("content-type"));
        String metadataString = GsonHelper.toPrettyJson(metadata);
        Files.write(objectMetadataFile, metadataString.getBytes("UTF-8"));
        logger.info("Updated " + objectMetadataFile);
        logger.info("metadata=" + metadataString);
        return (PutObjectS3Response) new PutObjectS3Response(s3Request).setHeader("ETag", eTag);
    }

}
