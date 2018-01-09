package us.cuatoi.s34jserver.core.operation.object;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.object.DeleteObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.DeleteObjectS3Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;

public class DeleteObjectS3RequestHandler extends ObjectS3RequestHandler<DeleteObjectS3Request, DeleteObjectS3Response> {
    public DeleteObjectS3RequestHandler(S3Context context, DeleteObjectS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    protected DeleteObjectS3Response handleObject() throws IOException {
        verifyObjectExists();
        PathHelper.deleteDir(objectMetadataDir);
        PathHelper.deleteDir(objectUploadDir);
        Files.delete(objectFile);
        logger.info("Deleted " + objectFile);
        return (DeleteObjectS3Response) new DeleteObjectS3Response(s3Request).setStatusCode(HttpServletResponse.SC_NO_CONTENT);
    }

}
