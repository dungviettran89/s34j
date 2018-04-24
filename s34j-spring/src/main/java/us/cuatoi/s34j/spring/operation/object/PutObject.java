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
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.helper.DateHelper;
import us.cuatoi.s34j.spring.model.*;
import us.cuatoi.s34j.spring.operation.ObjectManager;
import us.cuatoi.s34j.spring.operation.ObjectPartManager;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "PutObject")
public class PutObject extends AbstractBucketRule {

    public static final Logger logger = LoggerFactory.getLogger(PutObject.class);
    @Autowired
    private ObjectRepository objectRepository;
    @Autowired
    private ObjectPartRepository objectPartRepository;
    @Autowired
    private PartMappingRepository partMappingRepository;

    @Autowired
    private ObjectManager objectManager;
    @Autowired
    private ObjectPartManager objectPartManager;
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
        ObjectModel oldKey = objectManager.deleteCurrentVersion(objectName, bucketName);
        List<ObjectPartModel> objectPartModels = objectPartManager.saveToStore(parts);

        String version = DateHelper.format(SpringStorageConstants.X_AMZ_DATE_FORMAT, new Date()) +
                "-" + UUID.randomUUID().toString();
        final ObjectModel key = new ObjectModel();
        key.setObjectId(version);
        key.setObjectName(objectName);
        key.setCreatedDate(System.currentTimeMillis());
        key.setBucketName(bucketName);
        objectRepository.save(key);

        List<PartMappingModel> mappings = new ArrayList<>();
        int i = 0;
        for (ObjectPartModel pm : objectPartModels) {
            PartMappingModel pmm = new PartMappingModel();
            pmm.setMappingId(UUID.randomUUID().toString());
            pmm.setCreatedDate(System.currentTimeMillis());
            pmm.setKeyType("object");
            pmm.setKeyId(key.getObjectId());
            pmm.setPartName(pm.getPartName());
            pmm.setPartOrder(i);
            mappings.add(pmm);
            i++;
        }
        partMappingRepository.save(mappings);
        facts.put("statusCode", 200);
        facts.put("objectVersion", key.getObjectId());
        logger.info("perform(): parts.size=" + parts.size());
        logger.info("perform(): objectName=" + objectName);
        logger.info("perform(): bucketName=" + bucketName);
        logger.info("perform(): version=" + key.getObjectId());
        logger.info("perform(): oldKey=" + oldKey);
        logger.info("perform(): key=" + key);
    }


}
