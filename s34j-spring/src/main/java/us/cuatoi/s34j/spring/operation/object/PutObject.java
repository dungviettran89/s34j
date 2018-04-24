package us.cuatoi.s34j.spring.operation.object;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.model.ObjectRepository;
import us.cuatoi.s34j.spring.operation.ObjectManager;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

import java.io.InputStream;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "PutObject")
public class PutObject extends AbstractBucketRule {

    public static final Logger logger = LoggerFactory.getLogger(PutObject.class);
    @Autowired
    private ObjectRepository objectRepository;
    @Autowired
    private ObjectManager objectManager;
    @Condition
    public boolean shouldApply(Facts facts,
                               @Fact("PUT") boolean isPut,
                               @Fact("objectName") String objectName,
                               @Fact("bucketName") String bucketName) {
        return isPut &&
                isNotBlank(objectName) &&
                isNotBlank(bucketName) &&
                isBlank(facts.get("query:uploadId"));
    }

    @Action(order = 10)
    public void perform(Facts facts,
                        @Fact("parts") List<InputStream> parts,
                        @Fact("objectName") String objectName,
                        @Fact("bucketName") String bucketName) {
        objectManager.deleteCurrentVersionIfExists(objectName, bucketName);
        facts.put("errorCode", ErrorCode.NOT_IMPLEMENTED);
    }


}
