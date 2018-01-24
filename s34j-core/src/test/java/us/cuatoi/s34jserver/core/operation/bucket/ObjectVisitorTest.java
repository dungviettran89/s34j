package us.cuatoi.s34jserver.core.operation.bucket;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class ObjectVisitorTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Path baseDir = new File("").toPath();

    public ObjectVisitorTest() {
        logger.info("Base dir: " + baseDir);
    }

    @Test
    public void testVisit() throws Exception {
        test(3, null, null, null, null, null);
    }

    @Test
    public void testVisitWithPrefix() throws Exception {
        test(3, "src/main/java/us/cuatoi/s34jserver/core/operation/bucket", null, null, null, null);
    }

    @Test
    public void testVisitWithPrefixAndStartAfter() throws Exception {
        test(3,
                "src/main/java/us/cuatoi/s34jserver/core/operation/bucket",
                "src/main/java/us/cuatoi/s34jserver/core/operation/bucket/GetBucketLocationBucketS3RequestHandler.java", null, null, null);
    }

    @Test
    public void testVisitWithStartAfter() throws Exception {
        test(3,
                null,
                "src/main/java/us/cuatoi/s34jserver/core/operation/bucket/GetBucketLocationBucketS3RequestHandler.java", null, null, null);
    }

    @Test
    public void testVisitWithDelimiter() throws Exception {
        test(3,
                null,
                null,
                "/", null, null);
    }

    @Test
    public void testVisitWithSuffix() throws Exception {
        test(3, null, null, null, null, "DTO.java");
    }

    @Test
    public void testVisitWithDelimiterAndPrefix() throws Exception {
        test(3,
                "src/main/java/us/cuatoi/s34jserver/core/operation/",
                null,
                "/", null, null);
    }

    @Test
    public void testVisitWithDelimiterAndPrefixAndStartAfter() throws Exception {
        ObjectVisitor v1 = test(1, "src/main/java/us/cuatoi/s34jserver/core/operation/",
                null, "/", null, null);
        System.out.println(v1.getNextContinuationToken());
        v1 = test(1, "src/main/java/us/cuatoi/s34jserver/core/operation/",
                null, "/", v1.getNextContinuationToken(), null);
        System.out.println(v1.getNextContinuationToken());
        v1 = test(1, "src/main/java/us/cuatoi/s34jserver/core/operation/",
                null, "/", v1.getNextContinuationToken(), null);
        System.out.println(v1.getNextContinuationToken());
        v1 = test(1, "src/main/java/us/cuatoi/s34jserver/core/operation/",
                null, "/", v1.getNextContinuationToken(), null);
        System.out.println(v1.getNextContinuationToken());
    }

    private ObjectVisitor test(int maxKeys, String prefix, String startAfter, String delimiter, String continuationToken, String suffix) throws IOException {
        logger.debug("=========================================");
        logger.debug("maxKeys=" + maxKeys);
        logger.debug("prefix=" + prefix);
        logger.debug("startAfter=" + startAfter);
        logger.debug("delimiter=" + delimiter);
        ObjectVisitor visitor = new ObjectVisitor(baseDir)
                .setMaxKeys(maxKeys)
                .setPrefix(prefix)
                .setStartAfter(startAfter)
                .setDelimiter(delimiter)
                .setContinuationToken(continuationToken)
                .setSuffix(suffix)
                .visit();
        verify(visitor);
        logger.debug("=========================================");
        return visitor;
    }

    private void verify(ObjectVisitor visitor) {
        assertTrue("Should return less then max keys", visitor.getObjects().size() <= visitor.getMaxKeys());
        visitor.getObjects().forEach((p) -> {
            logger.trace("FOUND: " + p);
        });
        visitor.getPrefixes().forEach((p) -> {
            logger.trace("PREFIX: " + p);
        });
        logger.debug("TRUNCATED:" + visitor.isTruncated());
        logger.debug("Objects:" + visitor.getObjects());
        logger.debug("NextContinuationToken:" + visitor.getNextContinuationToken());
    }

}
