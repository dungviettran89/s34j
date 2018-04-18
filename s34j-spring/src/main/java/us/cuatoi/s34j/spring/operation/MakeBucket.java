package us.cuatoi.s34j.spring.operation;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.springframework.stereotype.Service;

@Service
@Rule(name = "MakeBucket")
public class MakeBucket implements ExecutionRule {
    @Condition
    public boolean shouldApply(@Fact("bucketName") String bucketName) {
        return false;
    }

    @Action
    public void makeBucket() {

    }
}
