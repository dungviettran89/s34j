package us.cuatoi.s34j.spring.operation.object;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.model.ObjectRepository;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

public abstract class AbstractObjectRule extends AbstractBucketRule {
    public static final Logger logger = LoggerFactory.getLogger(AbstractObjectRule.class);
    @Autowired
    private ObjectRepository objectRepository;

    @Action(order = -1)
    public void verifyObjectExists(Facts facts,
                                   @Fact("objectName") String objectName,
                                   @Fact("bucketName") String bucketName) {
        if (objectRepository.findOneByObjectNameAndBucketName(objectName, bucketName) == null) {
            logger.warn("verifyBucketExists() bucketName=" + bucketName);
            logger.warn("verifyBucketExists() objectName=" + objectName);
            logger.warn("verifyBucketExists() errorCode=" + ErrorCode.NO_SUCH_KEY);
            facts.put("errorCode", ErrorCode.NO_SUCH_KEY);
            throw new SpringStorageException(ErrorCode.NO_SUCH_KEY);
        }
    }
}
