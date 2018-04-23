package us.cuatoi.s34j.spring.operation.object;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.model.ObjectRepository;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

public abstract class AbstractObjectRule extends AbstractBucketRule {
    @Autowired
    private ObjectRepository objectRepository;

    @Action
    public void verifyObjectExists(Facts facts,
                                   @Fact("objectName") String objectName,
                                   @Fact("bucketName") String bucketName) {
        if (objectRepository.findOneByObjectNameAndBucketName(objectName, bucketName) == null) {
            facts.put("errorCode", ErrorCode.NO_SUCH_KEY);
            throw new SpringStorageException(ErrorCode.NO_SUCH_KEY);
        }
    }
}
