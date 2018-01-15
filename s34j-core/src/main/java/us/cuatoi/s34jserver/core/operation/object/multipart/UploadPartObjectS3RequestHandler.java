package us.cuatoi.s34jserver.core.operation.object.multipart;

import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.model.object.multipart.UploadPartObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.UploadPartObjectS3Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static us.cuatoi.s34jserver.core.S3Constants.ETAG_SUFFIX;

public class UploadPartObjectS3RequestHandler extends MultipartUploadObjectS3RequestHandler<UploadPartObjectS3Request, UploadPartObjectS3Response> {
    public UploadPartObjectS3RequestHandler(S3Context context, UploadPartObjectS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    protected UploadPartObjectS3Response handleObject() throws IOException {
        verifyUploadExists();
        String partNumber = s3Request.getQueryParameter("partNumber");
        verifyPartNumber(partNumber);
        Path partFile = uploadDir.resolve(partNumber);
        Files.copy(s3Request.getContent(), partFile, StandardCopyOption.REPLACE_EXISTING);

        Path eTagFile = uploadDir.resolve(partNumber + ETAG_SUFFIX);
        String ETag = calculateETag(s3Request.getContent());
        Files.write(eTagFile, ETag.getBytes(StandardCharsets.UTF_8));

        logger.info("Created " + partFile);
        return (UploadPartObjectS3Response) new UploadPartObjectS3Response(s3Request)
                .setHeader("ETag", ETag);
    }

    private void verifyPartNumber(String partNumber) {
        try {
            Integer.parseInt(partNumber);
        } catch (NumberFormatException ex) {
            throw new S3Exception(ErrorCode.INTERNAL_ERROR);
        }
    }

}
