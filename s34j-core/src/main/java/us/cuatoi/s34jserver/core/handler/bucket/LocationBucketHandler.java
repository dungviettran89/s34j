package us.cuatoi.s34jserver.core.handler.bucket;

import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.dto.LocationConstraintXml;
import us.cuatoi.s34jserver.core.handler.BaseHandler;

import static org.apache.commons.lang3.StringUtils.*;

public class LocationBucketHandler extends BucketHandler {
    protected LocationBucketHandler(StorageContext context, Request request) {
        super(context, request);
    }


    @Override
    protected String getName() {
        return "s3:GetBucketLocation";
    }

    @Override
    public Response handle() throws Exception {
        verifyBucketExists();
        switch (lowerCase(request.getMethod())) {
            case "get":
                return handleGet();
            default:
                throw new S3Exception(ErrorCode.NOT_IMPLEMENTED);
        }
    }

    private Response handleGet() {
        LocationConstraintXml response = new LocationConstraintXml();
        response.setRegion(context.getRegion());
        return new Response().setContent(response);
    }

    public static class Builder extends BucketHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isBlank(request.getObjectName());
            ok = ok && contains(request.getQueryString(), "location");
            return ok;
        }

        @Override
        public BaseHandler create(StorageContext context, Request request) {
            return new LocationBucketHandler(context, request);
        }
    }
}

