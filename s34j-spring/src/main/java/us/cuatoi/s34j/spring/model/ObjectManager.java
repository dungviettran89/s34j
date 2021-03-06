package us.cuatoi.s34j.spring.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            List<PartModel> deletedParts = partRepository.findAllByObjectVersionOrderByPartOrder(deletedVersion.getObjectVersion());
            partManager.deletePart(deletedParts);
            objectRepository.delete(deletedVersion);
            logger.info("deleteCurrentVersionIfExists() deletedVersion=" + deletedVersion);
            logger.info("deleteCurrentVersionIfExists() deletedParts=" + deletedParts);
        }
    }
}
