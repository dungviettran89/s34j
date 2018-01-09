package us.cuatoi.s34jserver.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.model.S3Request;

public class S3RequestVerifier {
    private S3Context context;
    private S3Request s3Request;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public S3RequestVerifier(S3Context context, S3Request s3Request) {
        this.context = context;
        this.s3Request = s3Request;
    }

    public void verify() {
        logger.debug("request=" + s3Request);
        verifySignature();
    }

    private void verifySignature() {

    }
}
