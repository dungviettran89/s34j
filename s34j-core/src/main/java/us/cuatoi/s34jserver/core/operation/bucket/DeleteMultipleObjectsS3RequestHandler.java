package us.cuatoi.s34jserver.core.operation.bucket;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.DeleteErrorDTO;
import us.cuatoi.s34jserver.core.dto.DeleteResultDTO;
import us.cuatoi.s34jserver.core.dto.DeletedDTO;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.bucket.DeleteMultipleObjectsS3Request;
import us.cuatoi.s34jserver.core.model.bucket.DeleteMultipleObjectsS3Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class DeleteMultipleObjectsS3RequestHandler
        extends BucketS3RequestHandler<DeleteMultipleObjectsS3Request, DeleteMultipleObjectsS3Response> {
    public DeleteMultipleObjectsS3RequestHandler(S3Context context, DeleteMultipleObjectsS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public DeleteMultipleObjectsS3Response handle() throws IOException {
        Set<DeletedDTO> deletedObjects = Sets.newConcurrentHashSet();
        Set<DeleteErrorDTO> errors = Sets.newConcurrentHashSet();
        s3Request.getDto().getObjects().stream().forEach((o) -> {
            try {
                Path objectFile = bucketDir.resolve(o.getKey());
                Path objectMetadataDir = bucketMetadataDir.resolve(o.getKey());
                Path objectUploadDir = bucketUploadDir.resolve(o.getKey());

                PathHelper.deleteDir(objectUploadDir);
                PathHelper.deleteDir(objectMetadataDir);
                Files.deleteIfExists(objectFile);
                logger.info("Deleted " + objectFile);
                //todo: Handle versions
                DeletedDTO deleted = new DeletedDTO();
                deleted.setKey(o.getKey());
                deleted.setVersionId(o.getVersionId());
                deletedObjects.add(deleted);
            } catch (IOException e) {
                logger.warn("Can not delete in bucket " + bucketName);
                logger.warn("Can not delete key" + o.getKey());
                DeleteErrorDTO error = new DeleteErrorDTO();
                ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
                error.setCode(errorCode.getName());
                error.setMessage(errorCode.getDescription());
                error.setKey(o.getKey());
                error.setVersionId(o.getVersionId());
                errors.add(error);
            }
        });
        DeleteResultDTO result = new DeleteResultDTO();
        result.setErrors(Lists.newArrayList(errors));
        if (!s3Request.getDto().isQuiet()) {
            result.setDeleted(Lists.newArrayList(deletedObjects));
        }
        return (DeleteMultipleObjectsS3Response) new DeleteMultipleObjectsS3Response(s3Request).setContent(result);
    }
}
