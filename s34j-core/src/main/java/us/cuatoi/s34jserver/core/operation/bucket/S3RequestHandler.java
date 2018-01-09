package us.cuatoi.s34jserver.core.operation.bucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.S3Response;

import java.io.IOException;
import java.nio.file.Path;

public abstract class S3RequestHandler<F extends S3Request, T extends S3Response> {
    protected final S3Context context;
    protected final F s3Request;
    protected final Path workingDir;
    protected final Path baseMetadataDir;
    protected final Path baseUploadDir;
    protected final Path baseDir;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public S3RequestHandler(S3Context context, F s3Request) {
        this.context = context;
        this.s3Request = s3Request;
        baseDir = context.getPath();
        workingDir = baseDir.resolve(".s34j");
        baseMetadataDir = workingDir.resolve("metadata");
        baseUploadDir = workingDir.resolve("upload");
    }

    public abstract T handle() throws IOException;
}
