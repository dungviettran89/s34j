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
import us.cuatoi.s34j.spring.model.*;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static us.cuatoi.s34j.spring.helper.StorageHelper.newVersion;

@Service
@Rule(name = "PutObject")
public class PutObject extends AbstractBucketRule {

    public static final Logger logger = LoggerFactory.getLogger(PutObject.class);
    @Autowired
    private ObjectRepository objectRepository;
    @Autowired
    private ObjectManager objectManager;
    @Autowired
    private PartManager partManager;
    @Autowired
    private PartRepository partRepository;

    @Condition
    public boolean shouldApply(Facts facts,
                               @Fact("PUT") boolean isPut,
                               @Fact("parts") List<InputStream> parts,
                               @Fact("objectName") String objectName,
                               @Fact("bucketName") String bucketName) {
        return isPut &&
                isNotBlank(objectName) &&
                isNotBlank(bucketName) &&
                parts.size() > 0 &&
                isBlank(facts.get("query:uploadId"));
    }

    @Action(order = 10)
    public void putObject(Facts facts,
                          @Fact("parts") List<InputStream> parts,
                          @Fact("objectName") String objectName,
                          @Fact("bucketName") String bucketName) {
        objectManager.deleteCurrentVersionIfExists(objectName, bucketName);
        String newVersion = newVersion();
        int partOrder = 1;
        ArrayList<PartModel> partModels = new ArrayList<>();
        long length = 0;
        for (PartModel model : partManager.savePart(parts)) {
            model.setObjectVersion(newVersion);
            model.setObjectName(objectName);
            model.setBucketName(bucketName);
            model.setPartOrder(partOrder++);
            model.setPartId(newVersion());
            partModels.add(model);
            length += model.getLength();
        }
        partRepository.save(partModels);
        ObjectModel objectModel = new ObjectModel();
        objectModel.setBucketName(bucketName);
        objectModel.setObjectName(objectName);
        objectModel.setObjectVersion(newVersion);
        objectModel.setCreatedDate(System.currentTimeMillis());
        objectModel.setLength(length);
        objectRepository.save(objectModel);
        facts.put("statusCode", 200);
        facts.put("ETag", objectModel.getObjectVersion());
        logger.info("putObject() objectModel=" + objectModel);
        logger.info("putObject() partModels=" + partModels);
    }
}
