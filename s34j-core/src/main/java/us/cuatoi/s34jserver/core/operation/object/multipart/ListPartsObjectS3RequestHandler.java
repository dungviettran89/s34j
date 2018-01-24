package us.cuatoi.s34jserver.core.operation.object.multipart;

import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.dto.InitiatorXml;
import us.cuatoi.s34jserver.core.dto.ListPartsResultXml;
import us.cuatoi.s34jserver.core.dto.OwnerXml;
import us.cuatoi.s34jserver.core.dto.PartResponseXml;
import us.cuatoi.s34jserver.core.helper.PathHelper;
import us.cuatoi.s34jserver.core.model.object.multipart.ListPartsObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.ListPartsObjectS3Response;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;
import static us.cuatoi.s34jserver.core.S3Constants.STORAGE_CLASS;
import static us.cuatoi.s34jserver.core.helper.NumberHelper.parseLong;

public class ListPartsObjectS3RequestHandler extends MultipartUploadObjectS3RequestHandler<ListPartsObjectS3Request, ListPartsObjectS3Response> {

    private final String encodingType;
    private final long maxParts;
    private final String partNumberMarker;

    public ListPartsObjectS3RequestHandler(S3Context context, ListPartsObjectS3Request s3Request) {
        super(context, s3Request);
        encodingType = s3Request.getQueryParameter("encoding-type");
        maxParts = parseLong(s3Request.getQueryParameter("max-parts"), 1000);
        partNumberMarker = s3Request.getQueryParameter("part-number-marker");
    }

    @Override
    protected ListPartsObjectS3Response handleObject() throws IOException {
        verifyUploadExists();
        logger.info("encodingType=" + encodingType);
        logger.info("maxParts=" + maxParts);
        logger.info("partNumberMarker=" + partNumberMarker);

        InitiatorXml id = new InitiatorXml();
        id.setDisplayName(context.getServerId());
        id.setId(context.getServerId());

        OwnerXml od = new OwnerXml();
        od.setDisplayName(context.getServerId());
        od.setId(context.getServerId());

        ListPartsResultXml dto = new ListPartsResultXml();
        dto.setMaxParts(maxParts);
        dto.setBucket(bucketName);
        dto.setEncodingType(encodingType);
        dto.setInitiator(id);
        dto.setOwner(od);
        dto.setPartNumberMarker(partNumberMarker);
        dto.setStorageClass(STORAGE_CLASS);
        Files.list(uploadDir).sorted(Comparator.naturalOrder())
                .forEach((part) -> {
                    String partNumber = part.getFileName().toString();
                    if (equalsIgnoreCase(partNumber, METADATA_JSON)) {
                        logger.trace("Skipped metadata " + partNumber);
                        return;
                    }
                    if (endsWith(partNumber, S3Constants.ETAG_SUFFIX)) {
                        logger.trace("Skipped etag " + partNumber);
                        return;
                    }
                    if (isNotBlank(partNumberMarker) && compare(partNumber, partNumber) <= 0) {
                        logger.trace("Skipped due to partNumberMarker" + partNumber);
                        return;
                    }
                    if (dto.getParts().size() < maxParts) {
                        PartResponseXml prd = new PartResponseXml();
                        prd.setPartNumber(Long.parseLong(partNumber));
                        prd.setLastModified(PathHelper.getLastModifiedStringUnchecked(part));
                        prd.seteTag(getOrCalculateETagUnchecked(part));
                        prd.setSize(PathHelper.sizeUnchecked(part));
                        dto.getParts().add(prd);
                        dto.setNextPartNumberMarker(partNumber);
                    } else {
                        logger.trace("Updated truncate = true due to part " + partNumber);
                        dto.setTruncated(true);
                    }
                });
        if (!dto.isTruncated()) {
            dto.setNextPartNumberMarker(null);
        }
        return (ListPartsObjectS3Response) new ListPartsObjectS3Response(s3Request).setContent(dto);
    }

}
