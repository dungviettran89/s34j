package us.cuatoi.s34jserver.core.handler.bucket;

import us.cuatoi.s34jserver.core.Request;
import us.cuatoi.s34jserver.core.Response;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.StorageContext;
import us.cuatoi.s34jserver.core.dto.CommonPrefixesXml;
import us.cuatoi.s34jserver.core.dto.ContentsXml;
import us.cuatoi.s34jserver.core.dto.ListBucketResultV1Xml;
import us.cuatoi.s34jserver.core.dto.OwnerXml;
import us.cuatoi.s34jserver.core.handler.BaseHandler;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;
import static us.cuatoi.s34jserver.core.helper.PathHelper.getLastModifiedString;

public class ListObjectsV1Handler extends BucketHandler {
    private final String delimiter;
    private final String encodingType;
    private final String marker;
    private final long maxKeys;
    private final String prefix;

    protected ListObjectsV1Handler(StorageContext context, Request request) {
        super(context, request);
        delimiter = request.getQueryParameter("delimiter");
        encodingType = request.getQueryParameter("encoding-type");
        marker = request.getQueryParameter("marker");
        maxKeys = parseLong(request.getQueryParameter("max-keys"), 1000);
        prefix = request.getQueryParameter("prefix");
    }

    @Override
    protected String getName() {
        return "s3:ListBucket";
    }

    @Override
    public Response handle() throws Exception {
        logger.debug("delimiter=" + delimiter);
        logger.debug("encodingType=" + encodingType);
        logger.debug("maxKeys=" + maxKeys);
        logger.debug("prefix=" + prefix);
        logger.debug("marker=" + marker);
        ObjectVisitor visitor = new ObjectVisitor(bucketDir)
                .setDelimiter(delimiter)
                .setMaxKeys(maxKeys)
                .setPrefix(prefix)
                .setStartAfter(marker)
                .visit();

        ListBucketResultV1Xml dto = new ListBucketResultV1Xml();
        dto.setName(bucketName);
        dto.setPrefix(prefix);
        dto.setMarker(marker);
        dto.setMaxKeys(maxKeys);
        dto.setEncodingType(encodingType);
        dto.setTruncated(visitor.isTruncated());
        for (String prefix : visitor.getPrefixes()) {
            CommonPrefixesXml cpd = new CommonPrefixesXml();
            cpd.setPrefix(prefix);
            dto.getCommonPrefixes().add(cpd);
        }
        for (Path path : visitor.getObjects()) {
            logger.trace("Found: " + path);
            OwnerXml od = new OwnerXml();
            od.setId(context.getServerId());
            od.setDisplayName(context.getServerId());
            ContentsXml cd = new ContentsXml();
            cd.setKey(path.toString());
            cd.setStorageClass(S3Constants.STORAGE_CLASS);
            cd.setOwner(od);
            cd.setLastModified(getLastModifiedString(path));
            cd.setSize(Files.size(path));
            cd.seteTag(getObjectETag(path));
            dto.setNextMarker(visitor.getNextMarker());
            dto.getContents().add(cd);
        }
        logger.debug("Size=" + dto.getContents().size());
        logger.debug("NextMarker=" + dto.getNextMarker());
        logger.debug("Truncated=" + dto.isTruncated());
        return new Response().setContent(dto).setContentType(S3Constants.CONTENT_TYPE_XML);
    }

    public static class Builder extends BucketHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isBlank(request.getObjectName());
            ok = ok && equalsIgnoreCase(request.getMethod(), "get");
            ok = ok && !equalsIgnoreCase(request.getQueryParameter("list-type"), "2");
            ok = ok && !containsAny(request.getQueryString(),
                    "location", "uploads", "policy", "delete");
            return ok;
        }

        @Override
        public BaseHandler create(StorageContext context, Request request) {
            return new ListObjectsV1Handler(context, request);
        }
    }
}
