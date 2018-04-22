package us.cuatoi.s34j.spring.operation.bucket;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Fact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import us.cuatoi.s34j.spring.operation.ExecutionRule;

public abstract class AbstractBucketRule implements ExecutionRule {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBucketRule.class);
    @Autowired
    private BucketVerifier bucketVerifier;

    @Action
    public void verifyBucketName(@Fact("bucketName") String bucketName) {
        if (bucketVerifier.verifyBucketName(bucketName)) {
            return;
        }
        logger.info("verifyBucketName() invalidName=" + bucketName);
    }

    @Action
    public void verifyBucketExists(@Fact("bucketName") String bucketName) {
        if (bucketVerifier.verifyBucketExists(bucketName)) {
            return;
        }
        logger.info("verifyBucketExists() bucketNotExists=" + bucketName);
    }
}
