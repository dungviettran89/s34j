package us.cuatoi.s34jserver.core.operation.object.multipart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.BaseEncoding;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.model.S3Response;
import us.cuatoi.s34jserver.core.model.object.ObjectS3Request;
import us.cuatoi.s34jserver.core.operation.object.ObjectS3RequestHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static us.cuatoi.s34jserver.core.S3Constants.ETAG_SUFFIX;

public abstract class MultipartUploadObjectS3RequestHandler<F extends ObjectS3Request, T extends S3Response> extends ObjectS3RequestHandler<F, T> {
    protected final String uploadId;
    protected final Path uploadDir;
    protected final Path uploadMetadataFile;

    public MultipartUploadObjectS3RequestHandler(S3Context context, F s3Request) {
        super(context, s3Request);
        uploadId = getUploadId(s3Request);

        uploadDir = objectUploadDir.resolve(uploadId);
        uploadMetadataFile = uploadDir.resolve(S3Constants.METADATA_JSON);
    }

    private String getUploadId(F s3Request) {
        String id = s3Request.getQueryParameter("uploadId");
        return isNotBlank(id) ? id : generateNewId();
    }

    @VisibleForTesting
    public static String generateNewId() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[24]);
        buffer.putLong(System.currentTimeMillis());
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return BaseEncoding.base32Hex().encode(buffer.array()).replace("=", "");
    }

    protected void verifyUploadExists() {
        if (!Files.exists(uploadDir)) {
            throw new S3Exception(ErrorCode.NO_SUCH_UPLOAD);
        }
    }

    protected String getOrCalculateETag(Path partFile) throws IOException {
        Path eTagFile = uploadDir.resolve(partFile.getFileName().toString() + ETAG_SUFFIX);
        String savedETag;
        if (Files.exists(eTagFile)) {
            savedETag = new String(Files.readAllBytes(eTagFile), UTF_8);
        } else {
            savedETag = calculateETag(partFile);
            Files.write(eTagFile, savedETag.getBytes(UTF_8));
        }
        return savedETag;
    }

    protected String getOrCalculateETagUnchecked(Path partFile) {
        try {
            return getOrCalculateETag(partFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
