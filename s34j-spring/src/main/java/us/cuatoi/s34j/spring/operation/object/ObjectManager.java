package us.cuatoi.s34j.spring.operation.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.DeletedObjectModel;
import us.cuatoi.s34j.spring.model.DeletedObjectRepository;
import us.cuatoi.s34j.spring.model.ObjectModel;
import us.cuatoi.s34j.spring.model.ObjectRepository;

import java.util.UUID;

@Service
public class ObjectManager {
    private static final Logger logger = LoggerFactory.getLogger(ObjectManager.class);
    @Autowired
    private ObjectRepository objectRepository;
    @Autowired
    private DeletedObjectRepository deletedObjectRepository;

    public ObjectModel deleteCurrentVersion(String objectName, String bucketName) {
        ObjectModel keyToDelete = objectRepository.findOneByObjectNameAndBucketName(objectName, bucketName);
        if (keyToDelete != null) {
            DeletedObjectModel oldVersionToDelete = new DeletedObjectModel();
            oldVersionToDelete.setId(UUID.randomUUID().toString());
            oldVersionToDelete.setObjectName(keyToDelete.getObjectName());
            oldVersionToDelete.setBucketName(keyToDelete.getBucketName());
            oldVersionToDelete.setVersionName(keyToDelete.getObjectVersion());
            oldVersionToDelete.setDeleteDate(System.currentTimeMillis());
            deletedObjectRepository.save(oldVersionToDelete);
            objectRepository.delete(keyToDelete);
            logger.info("deleteCurrentVersion() objectName=" + objectName);
            logger.info("deleteCurrentVersion() bucketName=" + bucketName);
            logger.info("deleteCurrentVersion() keyToDelete=" + keyToDelete);
        }
        return keyToDelete;
    }
}
