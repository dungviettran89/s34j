package us.cuatoi.s34jserver.core.handler.bucket;

import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.apache.commons.lang3.StringUtils.*;

public class PolicyBucketHandler extends BucketHandler {
    protected PolicyBucketHandler(StorageContext context, Request request) {
        super(context, request);
    }

    @Override
    public Response handle() throws Exception {
        if (equalsIgnoreCase(request.getMethod(), "get")) {
            return handlePolicyGet();
        } else if (equalsIgnoreCase(request.getMethod(), "put")) {
            return handlePolicyPut();
        } else if (equalsIgnoreCase(request.getMethod(), "delete")) {
            return handlePolicyDelete();
        }
        throw new S3Exception(ErrorCode.METHOD_NOT_ALLOWED);
    }

    private Response handlePolicyDelete() throws IOException {
        if (Files.deleteIfExists(bucketPolicyFile)) {
            logger.info("Deleted " + bucketPolicyFile);
        }
        return new Response().setStatus(204);
    }

    private Response handlePolicyPut() throws IOException {
        Files.copy(request.getContent(), bucketPolicyFile, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Updated " + bucketPolicyFile);
        return new Response().setStatus(204);
    }

    private Response handlePolicyGet() throws IOException {
        Object content = "{}";
        if (Files.exists(this.bucketPolicyFile)) {
            content = this.bucketPolicyFile;
        }
        return new Response()
                .setContentType("application/json; charset=UTF-8")
                .setContent(content);
    }

    public static class Builder extends BucketHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && !contains(request.getQueryString(), "policy");
            return ok;
        }

        @Override
        public BaseHandler create(SimpleStorageContext context, Request request) {
            return new PolicyBucketHandler(context, request);
        }
    }
}
