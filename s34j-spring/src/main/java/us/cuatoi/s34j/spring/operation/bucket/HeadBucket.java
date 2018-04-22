package us.cuatoi.s34j.spring.operation.bucket;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "HeadBucket")
public class HeadBucket extends AbstractBucketRule {
    @Condition
    public boolean shouldApply(
            @Fact("HEAD") boolean isHead,
            @Fact("bucketName") String bucketName) {
        return isHead && isNotBlank(bucketName);
    }

    @Action
    public void perform(Facts facts) {
        facts.put("statusCode", 200);
    }
}
