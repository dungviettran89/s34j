package us.cuatoi.s34jserver.core.operation.bucket;

import com.sun.javafx.charts.ChartLayoutAnimator;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.*;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.bucket.ListMultipartUploadsBucketS3Request;
import us.cuatoi.s34jserver.core.model.bucket.ListMultipartUploadsBucketS3Response;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;
import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;

public class ListMultipartUploadsBucketS3RequestHandler extends BucketS3RequestHandler<ListMultipartUploadsBucketS3Request, ListMultipartUploadsBucketS3Response> {

    private final String delimiter;
    private final String encodingType;
    private final String marker;
    private final long maxUploads;
    private final String prefix;
    private final String uploadIdMarker;

    public ListMultipartUploadsBucketS3RequestHandler(S3Context context, ListMultipartUploadsBucketS3Request s3Request) {
        super(context, s3Request);
        delimiter = s3Request.getQueryParameter("delimiter");
        encodingType = s3Request.getQueryParameter("encoding-type");
        marker = s3Request.getQueryParameter("marker");
        uploadIdMarker = s3Request.getQueryParameter("upload-id-marker");
        maxUploads = parseLong(s3Request.getQueryParameter("max-uploads"), 1000);
        prefix = s3Request.getQueryParameter("prefix");
    }

    @Override
    public ListMultipartUploadsBucketS3Response handle() throws IOException {
        logger.debug("delimiter=" + delimiter);
        logger.debug("encodingType=" + encodingType);
        logger.debug("maxUploads=" + maxUploads);
        logger.debug("prefix=" + prefix);
        logger.debug("marker=" + marker);
        logger.debug("uploadIdMarker=" + uploadIdMarker);
        String uploadMarker = marker;
        if (isNotBlank(uploadIdMarker)) {
            uploadMarker = uploadMarker + "/" + uploadIdMarker;
        } else {
            uploadMarker = uploadMarker + "/" + Character.MAX_VALUE;
        }
        ObjectVisitor visitor = new ObjectVisitor(bucketUploadDir)
                .setDelimiter(delimiter)
                .setMaxKeys(maxUploads)
                .setPrefix(prefix)
                .setStartAfter(uploadMarker)
                .setSuffix("/" + METADATA_JSON)
                .visit();

        ListMultipartUploadsResultDTO result = new ListMultipartUploadsResultDTO();
        result.setBucketName(bucketName);
        result.setMaxUploads(maxUploads);
        result.setKeyMarker(marker);
        result.setUploadIdMarker(uploadIdMarker);
        for (String prefix : visitor.getPrefixes()) {
            PrefixDTO pd = new PrefixDTO();
            pd.setPrefix(prefix);
            result.getCommonPrefixes().add(pd);
        }
        for (Path metadata : visitor.getObjects()) {
            Path upload = metadata.getParent();
            Path object = upload.getParent();
            OwnerDTO oo = new OwnerDTO();
            oo.setDisplayName(context.getServerId());
            oo.setId(context.getServerId());
            InitiatorDTO io = new InitiatorDTO();
            io.setDisplayName(context.getServerId());
            io.setId(context.getServerId());
            UploadDTO ud = new UploadDTO();
            ud.setOwner(oo);
            ud.setInitiator(io);
            ud.setStorageClass(S3Constants.STORAGE_CLASS);
            ud.setUploadId(upload.getFileName().toString());
            ud.setObjectName(object.getFileName().toString());
            ud.setInitiated(PathHelper.getCreationTimeString(metadata));
            result.getUploads().add(ud);
        }
        result.setTruncated(visitor.isTruncated());
        if (visitor.isTruncated()) {
            Path nextUploadMetadata = bucketDir.resolve(visitor.getNextMarker());
            Path nextUpload = nextUploadMetadata.getParent();
            Path nextObject = nextUpload.getParent();
            result.setNextKeyMarker(nextObject.getFileName().toString());
            result.setNextUploadIdMarker(nextUpload.getFileName().toString());
        }
        return (ListMultipartUploadsBucketS3Response) new ListMultipartUploadsBucketS3Response(s3Request)
                .setContent(result);
    }
}
