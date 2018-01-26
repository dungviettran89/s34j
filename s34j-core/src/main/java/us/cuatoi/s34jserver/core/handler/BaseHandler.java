package us.cuatoi.s34jserver.core.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.Request;
import us.cuatoi.s34jserver.core.Response;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.StorageContext;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import java.io.IOException;
import java.nio.file.Path;

public abstract class BaseHandler {

    protected final StorageContext context;
    protected final Request request;
    protected final Path baseDir;
    protected final Path baseMetadataDir;
    protected final Path baseUploadDir;
    protected final String separator;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected BaseHandler(StorageContext context, Request request) {
        this.context = context;
        this.request = request;
        this.baseDir = context.getBaseDir();
        this.separator = baseDir.getFileSystem().getSeparator();
        this.baseMetadataDir = baseDir.resolve(S3Constants.WORK_DIR).resolve(S3Constants.METADATA_DIR);
        this.baseUploadDir = baseDir.resolve(S3Constants.WORK_DIR).resolve(S3Constants.UPLOAD_DIR);
    }

    protected String getName() {
        return "Unknown";
    }

    public abstract Response handle() throws Exception ;

    public boolean needVerification() throws IOException, Exception {
        return true;
    }

    public abstract static class Builder {

        public boolean canHandle(Request request) {
            return false;
        }

        public abstract BaseHandler create(SimpleStorageContext context, Request request);
    }
}
