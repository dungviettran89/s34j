package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.CommonPrefixesDTO;
import us.cuatoi.s34jserver.core.dto.ContentsDTO;
import us.cuatoi.s34jserver.core.dto.ListBucketResultDTO;
import us.cuatoi.s34jserver.core.dto.OwnerDTO;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.model.bucket.ListObjectsV2S3Request;
import us.cuatoi.s34jserver.core.model.bucket.ListObjectsV2S3Response;
import us.cuatoi.s34jserver.core.model.object.ObjectMetadata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static us.cuatoi.s34jserver.core.S3Constants.EXPIRATION_DATE_FORMAT;
import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;
import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;
import static us.cuatoi.s34jserver.core.helper.PathHelper.md5HashFile;

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
        delimiter = s3Request.getHeader("delimiter");
        encodingType = s3Request.getHeader("encoding-type");
        maxKeys = parseLong(s3Request.getHeader("max-keys"), 1000);
        prefix = s3Request.getHeader("prefix");
        continuationToken = s3Request.getHeader("continuation-token");
        fetchOwner = s3Request.getHeader("fetch-owner");
        startAfter = s3Request.getHeader("start-after");
    }

    @Override
    public ListObjectsV2S3Response handle() throws IOException {
        logger.trace("delimiter=" + delimiter);
        logger.trace("encodingType=" + encodingType);
        logger.trace("maxKeys=" + maxKeys);
        logger.trace("prefix=" + prefix);
        logger.trace("continuationToken=" + continuationToken);
        logger.trace("fetchOwner=" + fetchOwner);
        logger.trace("startAfter=" + startAfter);

        ObjectVisitor visitor = new ObjectVisitor(bucketDir)
                .setDelimiter(delimiter)
                .setMaxKeys(maxKeys)
                .setPrefix(prefix)
                .setContinuationToken(continuationToken)
                .setStartAfter(startAfter)
                .visit();

        ListBucketResultDTO dto = new ListBucketResultDTO();
        dto.setName(bucketName);
        dto.setMaxKeys(maxKeys);
        dto.setPrefix(prefix);
        dto.setStartAfter(prefix);
        dto.setTruncated(visitor.isTruncated());
        dto.setKeyCount(visitor.getObjects().size());
        for (String prefix : visitor.getPrefixes()) {
            CommonPrefixesDTO cpd = new CommonPrefixesDTO();
            cpd.setPrefix(prefix);
            dto.getCommonPrefixes().add(cpd);
        }
        for (Path path : visitor.getObjects()) {
            logger.debug("Found :" + path);
            ContentsDTO cd = new ContentsDTO();
            cd.setKey(path.toString());
            cd.setStorageClass(S3Constants.STORAGE_CLASS);
            cd.setOwner(getOwner(path));
            cd.setLastModified(getLastModified(path));
            cd.setSize(getSize(path));
            cd.seteTag(getETag(path));
            dto.getContents().add(cd);
        }

        return (ListObjectsV2S3Response) new ListObjectsV2S3Response(s3Request).setContent(dto);
    }

    private long getSize(Path path) throws IOException {
        return Files.size(bucketDir.resolve(path));
    }

    private OwnerDTO getOwner(Path path) {
        if ("true".equalsIgnoreCase(fetchOwner)) {
            OwnerDTO od = new OwnerDTO();
            od.setId(context.getServerId());
            od.setDisplayName(context.getServerId());
            return od;
        }
        return null;
    }

    private String getLastModified(Path path) throws IOException {
        BasicFileAttributes attribute = Files.readAttributes(bucketDir.resolve(path), BasicFileAttributes.class);
        return EXPIRATION_DATE_FORMAT.print(attribute.lastModifiedTime().toMillis());
    }

    private String getETag(Path path) throws IOException {
        String eTag;
        Path metadataFile = bucketMetadataDir.resolve(path).resolve(METADATA_JSON);
        if (Files.exists(metadataFile)) {
            ObjectMetadata metadata = DTOHelper.fromJson(metadataFile, ObjectMetadata.class);
            eTag = metadata.geteTag();
        } else {
            eTag = md5HashFile(path);
            ObjectMetadata metadata = new ObjectMetadata().seteTag(eTag);
            Files.write(metadataFile, DTOHelper.toPrettyJson(metadata).getBytes(StandardCharsets.UTF_8));
        }
        return eTag;
    }

}
