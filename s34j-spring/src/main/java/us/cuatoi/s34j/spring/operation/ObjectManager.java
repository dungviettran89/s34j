package us.cuatoi.s34j.spring.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.ObjectModel;
import us.cuatoi.s34j.spring.model.ObjectRepository;
import us.cuatoi.s34j.spring.model.PartModel;
import us.cuatoi.s34j.spring.model.PartRepository;

import java.util.List;

@Service
public class ObjectManager {
    private static final Logger logger = LoggerFactory.getLogger(ObjectManager.class);
    @Autowired
    private ObjectRepository objectRepository;
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private PartManager partManager;

    public void deleteCurrentVersionIfExists(String objectName, String bucketName) {
        ObjectModel deletedVersion = objectRepository.findOneByObjectNameAndBucketName(objectName, bucketName);
        if (deletedVersion != null) {
            List<PartModel> deletedParts = partRepository.findAllByObjectVersion(deletedVersion.getObjectVersion());
            partManager.delete(deletedParts);
            objectRepository.delete(deletedVersion);
            logger.info("deleteCurrentVersionIfExists() deletedVersion=" + deletedVersion);
            logger.info("deleteCurrentVersionIfExists() deletedParts=" + deletedParts);
        }
    }
}
