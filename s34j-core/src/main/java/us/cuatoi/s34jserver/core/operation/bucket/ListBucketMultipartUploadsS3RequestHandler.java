package us.cuatoi.s34jserver.core.operation.bucket;

import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.*;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.bucket.ListBucketMultipartUploadsS3Request;
import us.cuatoi.s34jserver.core.model.bucket.ListBucketMultipartUploadsS3Response;

import java.io.IOException;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;
import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;

public class ListBucketMultipartUploadsS3RequestHandler extends BucketS3RequestHandler<ListBucketMultipartUploadsS3Request, ListBucketMultipartUploadsS3Response> {

    private final String delimiter;
    private final String encodingType;
    private final String marker;
    private final long maxUploads;
    private final String prefix;
    private final String uploadIdMarker;

    public ListBucketMultipartUploadsS3RequestHandler(S3Context context, ListBucketMultipartUploadsS3Request s3Request) {
        super(context, s3Request);
        delimiter = s3Request.getQueryParameter("delimiter");
        encodingType = s3Request.getQueryParameter("encoding-type");
        marker = s3Request.getQueryParameter("marker");
        uploadIdMarker = s3Request.getQueryParameter("upload-id-marker");
        maxUploads = parseLong(s3Request.getQueryParameter("max-uploads"), 1000);
        prefix = s3Request.getQueryParameter("prefix");

    }

    @Override
    public ListBucketMultipartUploadsS3Response handle() throws IOException {
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
        Path lastMetadataFile=null;
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
        return (ListBucketMultipartUploadsS3Response) new ListBucketMultipartUploadsS3Response(s3Request)
                .setContent(result);
    }
}
