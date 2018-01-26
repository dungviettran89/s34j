package us.cuatoi.s34jserver.core.handler.bucket;

import us.cuatoi.s34jserver.core.Request;
import us.cuatoi.s34jserver.core.Response;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.StorageContext;
import us.cuatoi.s34jserver.core.dto.*;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;
import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;

public class ListUploadsHandler extends BucketHandler {

    private final String delimiter;
    private final String encodingType;
    private final String marker;
    private final long maxUploads;
    private final String prefix;
    private final String uploadIdMarker;

    protected ListUploadsHandler(StorageContext context, Request request) {
        super(context, request);
        delimiter = request.getQueryParameter("delimiter");
        encodingType = request.getQueryParameter("encoding-type");
        marker = request.getQueryParameter("marker");
        uploadIdMarker = request.getQueryParameter("upload-id-marker");
        maxUploads = parseLong(request.getQueryParameter("max-uploads"), 1000);
        prefix = request.getQueryParameter("prefix");
    }

    @Override
    public Response handle() throws Exception {
        logger.debug("delimiter=" + delimiter);
        logger.debug("encodingType=" + encodingType);
        logger.debug("maxUploads=" + maxUploads);
        logger.debug("prefix=" + prefix);
        logger.debug("marker=" + marker);
        logger.debug("uploadIdMarker=" + uploadIdMarker);
        String uploadMarker = marker;
        if (isNotBlank(uploadMarker) && isNotBlank(uploadIdMarker)) {
            uploadMarker = uploadMarker + separator + uploadIdMarker;
        } else if (isNotBlank(uploadMarker)) {
            uploadMarker = uploadMarker + separator + Character.MAX_VALUE;
        }
        ObjectVisitor visitor = new ObjectVisitor(bucketUploadDir)
                .setDelimiter(delimiter)
                .setMaxKeys(maxUploads)
                .setPrefix(prefix)
                .setStartAfter(uploadMarker)
                .setSuffix(separator + METADATA_JSON)
                .visit();

        ListMultipartUploadsResultXml result = new ListMultipartUploadsResultXml();
        result.setBucketName(bucketName);
        result.setMaxUploads(maxUploads);
        result.setKeyMarker(marker);
        result.setUploadIdMarker(uploadIdMarker);
        for (String prefix : visitor.getPrefixes()) {
            PrefixXml pd = new PrefixXml();
            pd.setPrefix(prefix);
            result.getCommonPrefixes().add(pd);
        }
        Path lastMetadataFile = null;
        for (Path metadata : visitor.getObjects()) {
            Path upload = metadata.getParent();
            Path object = upload.getParent();
            OwnerXml oo = new OwnerXml();
            oo.setDisplayName(context.getServerId());
            oo.setId(context.getServerId());
            InitiatorXml io = new InitiatorXml();
            io.setDisplayName(context.getServerId());
            io.setId(context.getServerId());
            UploadXml ud = new UploadXml();
            ud.setOwner(oo);
            ud.setInitiator(io);
            ud.setStorageClass(S3Constants.STORAGE_CLASS);
            ud.setUploadId(upload.getFileName().toString());
            ud.setObjectName(object.getFileName().toString());
            ud.setInitiated(PathHelper.getCreationTimeString(metadata));
            result.getUploads().add(ud);
            lastMetadataFile = metadata;
        }
        result.setTruncated(visitor.isTruncated());
        if (visitor.isTruncated() && lastMetadataFile != null) {
            Path nextUploadMetadata = bucketDir.resolve(lastMetadataFile);
            Path nextUpload = nextUploadMetadata.getParent();
            Path nextObject = nextUpload.getParent();
            result.setNextKeyMarker(nextObject.getFileName().toString());
            result.setNextUploadIdMarker(nextUpload.getFileName().toString());
        }
        return new Response().setContent(result);
    }

    public static class Builder extends BaseHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isBlank(request.getObjectName());
            ok = ok && equalsIgnoreCase(request.getMethod(), "get");
            ok = ok && contains(request.getQueryString(), "uploads");
            return ok;
        }

        @Override
        public BaseHandler create(SimpleStorageContext context, Request request) {
            return new ListUploadsHandler(context, request);
        }
    }
}
