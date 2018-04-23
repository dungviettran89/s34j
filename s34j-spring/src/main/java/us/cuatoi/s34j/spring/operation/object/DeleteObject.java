package us.cuatoi.s34j.spring.operation.object;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "DeleteObject")
public class DeleteObject extends AbstractObjectRule {

    @Autowired
    private ObjectManager objectManager;

    @Condition
    public boolean shouldApply(
            @Fact("DELETE") boolean isDeleting,
            @Fact("objectName") String objectName,
            @Fact("bucketName") String bucketName) {
        return isDeleting && isNotBlank(objectName) && isNotBlank(bucketName);
    }

    @Action(order = 10)
    public void perform(Facts facts,
                        @Fact("objectName") String objectName,
                        @Fact("bucketName") String bucketName) {
        objectManager.deleteCurrentVersion(objectName, bucketName);
        facts.put("statusCode", 200);
    }
}
