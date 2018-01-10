package us.cuatoi.s34jserver.core.operation.object;

import us.cuatoi.s34jserver.core.S3Context;
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
        Files.createDirectories(objectFile.getParent());
        Files.copy(s3Request.getContent(), objectFile);
        logger.info("Saved " + objectFile);
        Files.createDirectories(objectUploadDir);
        logger.info("Created " + objectUploadDir);


        String eTag = calculateETag();
        ObjectMetadata metadata = createMetadata(eTag);
        saveMetadata(metadata);
        return (PutObjectS3Response) new PutObjectS3Response(s3Request).setHeader("ETag", eTag);
    }

}
