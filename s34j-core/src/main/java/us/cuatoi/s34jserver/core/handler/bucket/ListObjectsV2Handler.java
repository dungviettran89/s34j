package us.cuatoi.s34jserver.core.handler.bucket;

import us.cuatoi.s34jserver.core.Request;
import us.cuatoi.s34jserver.core.Response;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.StorageContext;
import us.cuatoi.s34jserver.core.dto.CommonPrefixesXml;
import us.cuatoi.s34jserver.core.dto.ContentsXml;
import us.cuatoi.s34jserver.core.dto.ListBucketResultV2Xml;
import us.cuatoi.s34jserver.core.dto.OwnerXml;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.operation.bucket.ObjectVisitor;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;

public class ListObjectsV2Handler extends BucketHandler {

    private final String delimiter;
    private final String encodingType;
    private final long maxKeys;
    private final String prefix;
    private final String continuationToken;
    private final String fetchOwner;
    private final String startAfter;
    
    protected ListObjectsV2Handler(StorageContext context, Request request) {
        super(context, request);
        delimiter = request.getQueryParameter("delimiter");
        encodingType = request.getQueryParameter("encoding-type");
        maxKeys = parseLong(request.getQueryParameter("max-keys"), 1000);
        prefix = request.getQueryParameter("prefix");
        continuationToken = request.getQueryParameter("continuation-token");
        fetchOwner = request.getQueryParameter("fetch-owner");
        startAfter = request.getQueryParameter("start-after");
    }

    @Override
    public Response handle() throws Exception {
        logger.debug("delimiter=" + delimiter);
        logger.debug("encodingType=" + encodingType);
        logger.debug("maxKeys=" + maxKeys);
        logger.debug("prefix=" + prefix);
        logger.debug("continuationToken=" + continuationToken);
        logger.debug("fetchOwner=" + fetchOwner);
        logger.debug("startAfter=" + startAfter);

        ObjectVisitor visitor = new ObjectVisitor(bucketDir)
                .setDelimiter(delimiter)
                .setMaxKeys(maxKeys)
                .setPrefix(prefix)
                .setContinuationToken(continuationToken)
                .setStartAfter(startAfter)
                .visit();

        ListBucketResultV2Xml dto = new ListBucketResultV2Xml();
        dto.setName(bucketName);
        dto.setMaxKeys(maxKeys);
        dto.setPrefix(prefix);
        dto.setStartAfter(startAfter);
        dto.setTruncated(visitor.isTruncated());
        dto.setKeyCount(visitor.getObjects().size());
        dto.setContinuationToken(continuationToken);
        dto.setEncodingType(encodingType);
        dto.setNextContinuationToken(visitor.getNextContinuationToken());
        for (String prefix : visitor.getPrefixes()) {
            CommonPrefixesXml cpd = new CommonPrefixesXml();
            cpd.setPrefix(prefix);
            dto.getCommonPrefixes().add(cpd);
        }
        for (Path path : visitor.getObjects()) {
            logger.trace("Found: " + path);
            ContentsXml cd = new ContentsXml();
            cd.setKey(path.toString());
            cd.setStorageClass(S3Constants.STORAGE_CLASS);
            cd.setOwner(getOwner(path));
            cd.setLastModified(PathHelper.getLastModifiedString(path));
            cd.setSize(Files.size(path));
            cd.seteTag(getObjectETag(path));
            dto.getContents().add(cd);
        }
        logger.debug("KeyCount=" + dto.getKeyCount());
        logger.debug("NextContinuationToken=" + dto.getNextContinuationToken());
        logger.debug("Truncated=" + dto.isTruncated());
        return new Response().setContent(dto).setContentType(S3Constants.CONTENT_TYPE_XML);
    }

    private OwnerXml getOwner(Path path) {
        if ("true".equalsIgnoreCase(fetchOwner)) {
            OwnerXml od = new OwnerXml();
            od.setId(context.getServerId());
            od.setDisplayName(context.getServerId());
            return od;
        }
        return null;
    }

    public static class Builder extends BucketHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isBlank(request.getObjectName());
            ok = ok && equalsIgnoreCase(request.getMethod(), "get");
            ok = ok && equalsIgnoreCase(request.getQueryParameter("list-type"), "2");
            return ok;
        }

        @Override
        public BaseHandler create(SimpleStorageContext context, Request request) {
            return new ListObjectsV2Handler(context, request);
        }
    }
}
