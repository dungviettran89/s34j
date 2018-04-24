package us.cuatoi.s34j.spring.operation.object;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.helper.StorageHelper;
import us.cuatoi.s34j.spring.model.ObjectModel;
import us.cuatoi.s34j.spring.model.ObjectRepository;

import java.util.Date;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "HeadObject")
public class HeadObject extends AbstractObjectRule {

    @Autowired
    private ObjectRepository objectRepository;

    @Condition
    public boolean shouldApply(Facts facts,
                               @Fact("HEAD") boolean isHead,
                               @Fact("objectName") String objectName,
                               @Fact("bucketName") String bucketName) {
        return isHead &&
                isNotBlank(objectName) &&
                isNotBlank(bucketName);
    }

    @Action
    public void headObject(Facts facts, @Fact("objectName") String objectName, @Fact("bucketName") String bucketName) {
        ObjectModel objectModel = objectRepository.findOneByObjectNameAndBucketName(objectName, bucketName);
        facts.put("statusCode", 200);
        facts.put("responseHeader:contentLength", objectModel.getLength());
        facts.put("responseHeader:Last-Modified",
                StorageHelper.format(SpringStorageConstants.HTTP_HEADER_DATE_FORMAT, new Date(objectModel.getCreatedDate())));
    }
}
