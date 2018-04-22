package us.cuatoi.s34j.spring.operation.object;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "PutObject")
public class PutObject extends AbstractBucketRule {
    @Condition
    public boolean shouldApply(
            @Fact("PUT") boolean isPut,
            @Fact("objectName") String objectName,
            @Fact("bucketName") String bucketName) {
        return isPut && isNotBlank(objectName) && isNotBlank("bucketName");
    }

    @Action
    public void perform() {
        throw new SpringStorageException(ErrorCode.NOT_IMPLEMENTED);
    }
}
