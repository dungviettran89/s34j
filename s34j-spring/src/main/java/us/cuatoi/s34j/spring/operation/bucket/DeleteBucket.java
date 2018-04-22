package us.cuatoi.s34j.spring.operation.bucket;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.BucketModel;
import us.cuatoi.s34j.spring.model.BucketRepository;
import us.cuatoi.s34j.spring.model.DeletedBucketModel;
import us.cuatoi.s34j.spring.model.DeletedBucketRepository;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "DeleteBucket")
public class DeleteBucket extends AbstractBucketRule {

    public static final Logger logger = LoggerFactory.getLogger(DeleteBucket.class);
    @Autowired
    private BucketRepository bucketRepository;
    @Autowired
    private DeletedBucketRepository deletedBucketRepository;
    @Autowired
    private BucketVerifier bucketVerifier;

    @Condition
    public boolean isDeleting(@Fact("DELETE") boolean isDelete,
                              @Fact("bucketName") String bucketName) {
        return isDelete && isNotBlank(bucketName);

    }

    @Action
    public void doDelete(Facts facts, @Fact("bucketName") String bucketName) {
        BucketModel bucketToDelete = bucketRepository.findOne(bucketName);
        logger.info("doDelete() bucketToDelete=" + bucketToDelete);
        ModelMapper mapper = new ModelMapper();
        DeletedBucketModel deletedBucket = mapper.map(bucketToDelete, DeletedBucketModel.class);
        deletedBucket.setDeleteDate(System.currentTimeMillis());
        deletedBucketRepository.save(deletedBucket);
        bucketRepository.delete(bucketToDelete);
        logger.info("doDelete() deletedBucket=" + deletedBucket);
        facts.put("statusCode", 200);
    }
}

