package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.CommonPrefixesXml;
import us.cuatoi.s34jserver.core.dto.ContentsXml;
import us.cuatoi.s34jserver.core.dto.ListBucketResultV2Xml;
import us.cuatoi.s34jserver.core.dto.OwnerXml;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.bucket.ListObjectsV2S3Request;
import us.cuatoi.s34jserver.core.model.bucket.ListObjectsV2S3Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;

public class ListObjectsV2S3RequestHandler extends BucketS3RequestHandler<ListObjectsV2S3Request, ListObjectsV2S3Response> {

    private final String delimiter;
    private final String encodingType;
    private final long maxKeys;
    private final String prefix;
    private final String continuationToken;
    private final String fetchOwner;
    private final String startAfter;

    public ListObjectsV2S3RequestHandler(S3Context context, ListObjectsV2S3Request s3Request) {
        super(context, s3Request);
        delimiter = s3Request.getQueryParameter("delimiter");
        encodingType = s3Request.getQueryParameter("encoding-type");
        maxKeys = parseLong(s3Request.getQueryParameter("max-keys"), 1000);
        prefix = s3Request.getQueryParameter("prefix");
        continuationToken = s3Request.getQueryParameter("continuation-token");
        fetchOwner = s3Request.getQueryParameter("fetch-owner");
        startAfter = s3Request.getQueryParameter("start-after");
    }

    @Override
    public ListObjectsV2S3Response handle() throws IOException {
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
        return (ListObjectsV2S3Response) new ListObjectsV2S3Response(s3Request).setContent(dto);
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

}
