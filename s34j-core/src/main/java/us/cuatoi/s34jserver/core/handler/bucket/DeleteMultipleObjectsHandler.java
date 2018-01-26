package us.cuatoi.s34jserver.core.handler.bucket;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.dto.DeleteErrorXml;
import us.cuatoi.s34jserver.core.dto.DeleteResultXml;
import us.cuatoi.s34jserver.core.dto.DeleteXml;
import us.cuatoi.s34jserver.core.dto.DeletedXml;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.*;

public class DeleteMultipleObjectsHandler extends BucketHandler {
    protected DeleteMultipleObjectsHandler(StorageContext context, Request request) {
        super(context, request);
    }

    @Override
    public Response handle() throws Exception {
        DeleteXml dto = DTOHelper.parseXmlContent(request.getContent(), new DeleteXml());
        Set<DeletedXml> deletedObjects = Sets.newConcurrentHashSet();
        Set<DeleteErrorXml> errors = Sets.newConcurrentHashSet();
        dto.getObjects().stream().forEach((o) -> {
            try {
                Path objectFile = bucketDir.resolve(o.getKey());
                Path objectMetadataDir = bucketMetadataDir.resolve(o.getKey());
                Path objectUploadDir = bucketUploadDir.resolve(o.getKey());

                PathHelper.deleteDir(objectUploadDir);
                PathHelper.deleteDir(objectMetadataDir);
                Files.deleteIfExists(objectFile);
                logger.info("Deleted " + objectFile);
                //todo: Handle versions
                DeletedXml deleted = new DeletedXml();
                deleted.setKey(o.getKey());
                deleted.setVersionId(o.getVersionId());
                deletedObjects.add(deleted);
            } catch (IOException e) {
                logger.warn("Can not delete in bucket " + bucketName);
                logger.warn("Can not delete key" + o.getKey());
                DeleteErrorXml error = new DeleteErrorXml();
                ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
                error.setCode(errorCode.getName());
                error.setMessage(errorCode.getDescription());
                error.setKey(o.getKey());
                error.setVersionId(o.getVersionId());
                errors.add(error);
            }
        });
        DeleteResultXml result = new DeleteResultXml();
        result.setErrors(Lists.newArrayList(errors));
        if (!dto.isQuiet()) {
            result.setDeleted(Lists.newArrayList(deletedObjects));
        }
        return new Response().setContent(result).setContentType(S3Constants.CONTENT_TYPE_XML);
    }

    public static class Builder extends BucketHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isBlank(request.getObjectName());
            ok = ok && equalsIgnoreCase(request.getMethod(), "post");
            ok = ok && contains(request.getQueryString(), "delete");
            return ok;
        }

        @Override
        public BaseHandler create(SimpleStorageContext context, Request request) {
            return new DeleteMultipleObjectsHandler(context, request);
        }
    }
}
