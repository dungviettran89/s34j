package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.*;
import us.cuatoi.s34jserver.core.model.bucket.ListObjectsV1S3Request;
import us.cuatoi.s34jserver.core.model.bucket.ListObjectsV1S3Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;
import static us.cuatoi.s34jserver.core.helper.PathHelper.getLastModifiedString;

public class ListObjectsV1S3RequestHandler extends BucketS3RequestHandler<ListObjectsV1S3Request,ListObjectsV1S3Response> {

    private final String delimiter;
    private final String encodingType;
    private final String marker;
    private final long maxKeys;
    private final String prefix;

    public ListObjectsV1S3RequestHandler(S3Context context, ListObjectsV1S3Request s3Request) {
        super(context, s3Request);
        delimiter = s3Request.getQueryParameter("delimiter");
        encodingType = s3Request.getQueryParameter("encoding-type");
        marker = s3Request.getQueryParameter("marker");
        maxKeys = parseLong(s3Request.getQueryParameter("max-keys"), 1000);
        prefix = s3Request.getQueryParameter("prefix");
    }

    @Override
    public ListObjectsV1S3Response handle() throws IOException {
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

        ListBucketResultV1DTO dto = new ListBucketResultV1DTO();
        dto.setName(bucketName);
        dto.setPrefix(prefix);
        dto.setMarker(marker);
        dto.setMaxKeys(maxKeys);
        dto.setEncodingType(encodingType);
        dto.setTruncated(visitor.isTruncated());
        for (String prefix : visitor.getPrefixes()) {
            CommonPrefixesDTO cpd = new CommonPrefixesDTO();
            cpd.setPrefix(prefix);
            dto.getCommonPrefixes().add(cpd);
        }
        for (Path path : visitor.getObjects()) {
            logger.trace("Found: " + path);
            OwnerDTO od = new OwnerDTO();
            od.setId(context.getServerId());
            od.setDisplayName(context.getServerId());
            ContentsDTO cd = new ContentsDTO();
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
        return (ListObjectsV1S3Response) new ListObjectsV1S3Response(s3Request).setContent(dto);
    }
}
