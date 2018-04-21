package us.cuatoi.s34j.spring.operation.bucket;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.BucketModel;
import us.cuatoi.s34j.spring.model.BucketRepository;
import us.cuatoi.s34j.spring.operation.ExecutionRule;

import static javax.servlet.http.HttpServletResponse.SC_OK;

@Service
@Rule(name = "MakeBucket")
public class MakeBucket implements ExecutionRule {

    @Autowired
    private BucketRepository bucketRepository;
    @Autowired
    private BucketVerifier bucketVerifier;

    @Condition
    public boolean shouldApply(
            @Fact("PUT") boolean isPut,
            @Fact("awsAccessKey") String awsAccessKey,
            @Fact("bucketName") String bucketName) {
        return bucketVerifier.verifyBucketName(bucketName) &&
                bucketVerifier.verifyBucketNotExist(bucketName);
    }

    @Action
    public void makeBucket(Facts facts,
                           @Fact("awsAccessKey") String awsAccessKey,
                           @Fact("bucketName") String bucketName,
                           @Fact("region") String region) {
        BucketModel bucket = new BucketModel();
        bucket.setName(bucketName);
        bucket.setOwner(awsAccessKey);
        bucket.setCreatedDate(System.currentTimeMillis());
        bucket.setLocation(region);
        bucketRepository.save(bucket);
        facts.put("statusCode", SC_OK);
    }


}
