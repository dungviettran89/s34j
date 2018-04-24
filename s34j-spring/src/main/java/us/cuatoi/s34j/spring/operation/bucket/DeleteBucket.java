package us.cuatoi.s34j.spring.operation.bucket;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.BucketModel;
import us.cuatoi.s34j.spring.model.BucketRepository;
import us.cuatoi.s34j.spring.model.DeletedObjectModel;
import us.cuatoi.s34j.spring.model.DeletedObjectRepository;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "DeleteBucket")
public class DeleteBucket extends AbstractBucketRule {

    public static final Logger logger = LoggerFactory.getLogger(DeleteBucket.class);
    @Autowired
    private BucketRepository bucketRepository;
    @Autowired
    private DeletedObjectRepository deletedObjectRepository;

    @Condition
    public boolean isDeleting(Facts facts,
                              @Fact("DELETE") boolean isDelete,
                              @Fact("bucketName") String bucketName) {
        return isDelete && isNotBlank(bucketName) && isBlank(facts.get("objectName"));

    }

    @Action(order = 10)
    public void doDelete(Facts facts, @Fact("bucketName") String bucketName) {
        BucketModel bucketToDelete = bucketRepository.findOne(bucketName);
        logger.info("doDelete() bucketToDelete=" + bucketToDelete);

        DeletedObjectModel deletedBucket = new DeletedObjectModel();
        deletedBucket.setDeleteId(UUID.randomUUID().toString());
        deletedBucket.setType("bucket");
        deletedBucket.setBucketName(bucketToDelete.getBucketName());
        deletedBucket.setDeleteDate(System.currentTimeMillis());
        bucketRepository.delete(bucketToDelete);

        deletedObjectRepository.save(deletedBucket);
        logger.info("doDelete() deletedBucket=" + deletedBucket);
        facts.put("statusCode", 200);
    }
}

