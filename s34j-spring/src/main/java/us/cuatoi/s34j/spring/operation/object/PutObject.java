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
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.helper.DateHelper;
import us.cuatoi.s34j.spring.model.*;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;
import us.cuatoi.s34j.spring.storage.block.BlockStorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "PutObject")
public class PutObject extends AbstractBucketRule {

    public static final Logger logger = LoggerFactory.getLogger(PutObject.class);
    @Autowired
    private ObjectRepository objectRepository;
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private PartMappingRepository partMappingRepository;
    @Autowired
    private BlockStorage blockStorage;
    @Autowired
    private DeletedObjectRepository deletedObjectRepository;

    @Condition
    public boolean shouldApply(
            @Fact("PUT") boolean isPut,
            @Fact("objectName") String objectName,
            @Fact("bucketName") String bucketName) {
        return isPut && isNotBlank(objectName) && isNotBlank(bucketName);
    }

    @Action
    public void perform(Facts facts,
                        @Fact("parts") List<InputStream> parts,
                        @Fact("objectName") String objectName,
                        @Fact("bucketName") String bucketName) {
        ObjectModel oldKey = objectRepository.findOneByObjectNameAndBucketName(objectName, bucketName);
        if (oldKey != null) {
            DeletedObjectModel oldVersionToDelete = new DeletedObjectModel();
            oldVersionToDelete.setId(UUID.randomUUID().toString());
            oldVersionToDelete.setObjectName(oldKey.getObjectName());
            oldVersionToDelete.setBucketName(oldKey.getBucketName());
            oldVersionToDelete.setVersionName(oldKey.getObjectVersion());
            oldVersionToDelete.setDeleteDate(System.currentTimeMillis());
            deletedObjectRepository.save(oldVersionToDelete);
            objectRepository.delete(oldKey);
        }

        List<PartModel> partModels = parts.parallelStream()
                .map(this::saveToBlock)
                .collect(Collectors.toList());
        partRepository.save(partModels);

        String version = DateHelper.format(SpringStorageConstants.X_AMZ_DATE_FORMAT, new Date()) +
                "-" + UUID.randomUUID().toString();
        final ObjectModel key = new ObjectModel();
        key.setObjectId(UUID.randomUUID().toString());
        key.setObjectName(objectName);
        key.setCreatedDate(System.currentTimeMillis());
        key.setBucketName(bucketName);
        key.setObjectVersion(version);
        objectRepository.save(key);

        List<PartMappingModel> mappings = new ArrayList<>();
        int i = 0;
        for (PartModel pm : partModels) {
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
        facts.put("objectVersion", key.getObjectVersion());
        logger.info("perform(): parts.size=" + parts.size());
        logger.info("perform(): objectName=" + objectName);
        logger.info("perform(): bucketName=" + bucketName);
        logger.info("perform(): version=" + key.getObjectVersion());
        logger.info("perform(): oldKey=" + oldKey);
        logger.info("perform(): key=" + key);
    }

    private PartModel saveToBlock(InputStream is) {
        String name = UUID.randomUUID().toString();
        try (InputStream i = is) {
            PartModel model = new PartModel();
            model.setPartName(name);
            model.setCreatedDate(System.currentTimeMillis());
            blockStorage.save(model.getPartName(), i);
            logger.info("saveToBlock() is=" + is);
            logger.info("saveToBlock() name=" + name);
            return model;
        } catch (IOException saveError) {
            logger.error("saveToBlock() is=" + is);
            logger.error("saveToBlock() name=" + name);
            logger.error("saveToBlock() saveError=" + saveError, saveError);
            throw new SpringStorageException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
