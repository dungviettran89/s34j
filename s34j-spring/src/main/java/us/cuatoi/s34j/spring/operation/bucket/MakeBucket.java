package us.cuatoi.s34j.spring.operation.bucket;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.model.BucketModel;
import us.cuatoi.s34j.spring.model.BucketRepository;
import us.cuatoi.s34j.spring.operation.ExecutionRule;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "MakeBucket")
public class MakeBucket implements ExecutionRule {

    @Autowired
    private BucketRepository bucketRepository;

    @Condition
    public boolean shouldApply(Facts facts,
                               @Fact("PUT") boolean isPut,
                               @Fact("awsAccessKey") String awsAccessKey,
                               @Fact("bucketName") String bucketName) {
        return isPut && isBlank(facts.get("objectName")) && isNotBlank(awsAccessKey);
    }

    @Action
    public void makeBucket(Facts facts,
                           @Fact("awsAccessKey") String awsAccessKey,
                           @Fact("bucketName") String bucketName,
                           @Fact("region") String region) {

        if (bucketRepository.findOne(bucketName) != null) {
            facts.put("errorCode", ErrorCode.BUCKET_ALREADY_EXISTS);
            throw new SpringStorageException(ErrorCode.BUCKET_ALREADY_EXISTS);
        }

        BucketModel bucket = new BucketModel();
        bucket.setBucketName(bucketName);
        bucket.setOwner(awsAccessKey);
        bucket.setCreatedDate(System.currentTimeMillis());
        bucket.setLocation(region);
        bucketRepository.save(bucket);
        facts.put("statusCode", SC_OK);
    }


}
