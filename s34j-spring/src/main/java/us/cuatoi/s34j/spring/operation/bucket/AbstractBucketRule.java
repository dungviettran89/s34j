package us.cuatoi.s34j.spring.operation.bucket;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.model.BucketRepository;
import us.cuatoi.s34j.spring.operation.ExecutionRule;

import static us.cuatoi.s34j.spring.dto.ErrorCode.NO_SUCH_BUCKET;

public abstract class AbstractBucketRule implements ExecutionRule {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBucketRule.class);
    @Autowired
    private BucketRepository bucketRepository;

    @Action(order = -1)
    public void verifyBucketExists(Facts facts, @Fact("bucketName") String bucketName) {
        if (bucketRepository.findOne(bucketName) == null) {
            logger.warn("verifyBucketExists() bucketName=" + bucketName);
            logger.warn("verifyBucketExists() errorCode=" + NO_SUCH_BUCKET);
            facts.put("errorCode", NO_SUCH_BUCKET);
            throw new SpringStorageException(NO_SUCH_BUCKET);
        }
    }
}
