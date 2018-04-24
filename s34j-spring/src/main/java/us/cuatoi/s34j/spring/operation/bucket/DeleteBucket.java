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
import us.cuatoi.s34j.spring.model.*;
import us.cuatoi.s34j.spring.operation.PartManager;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "DeleteBucket")
public class DeleteBucket extends AbstractBucketRule {

    public static final Logger logger = LoggerFactory.getLogger(DeleteBucket.class);
    @Autowired
    private BucketRepository bucketRepository;
    @Autowired
    private ObjectRepository objectRepository;
    @Autowired
    private DeletedPartRepository deletedPartRepository;
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private UploadRepository uploadRepository;
    @Autowired
    private UploadPartRepository uploadPartRepository;
    @Autowired
    private PartManager partManager;

    @Condition
    public boolean isDeleting(Facts facts,
                              @Fact("DELETE") boolean isDelete,
                              @Fact("bucketName") String bucketName) {
        return isDelete && isNotBlank(bucketName) && isBlank(facts.get("objectName"));

    }

    @Action
    public void doDelete(Facts facts, @Fact("bucketName") String bucketName) {
        BucketModel bucketToDelete = bucketRepository.findOne(bucketName);
        bucketRepository.delete(bucketToDelete);

        objectRepository.deleteByBucketName(bucketName);
        uploadRepository.deleteByBucketName(bucketName);
        uploadPartRepository.deleteByBucketName(bucketName);
        List<PartModel> parts = partRepository.findAllByBucketName(bucketName);
        partManager.delete(parts);
        facts.put("statusCode", 200);
    }
}

