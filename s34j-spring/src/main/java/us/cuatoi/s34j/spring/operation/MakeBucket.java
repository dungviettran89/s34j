package us.cuatoi.s34j.spring.operation;

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

import static javax.servlet.http.HttpServletResponse.SC_OK;

@Service
@Rule(name = "MakeBucket")
public class MakeBucket implements ExecutionRule {

    @Autowired
    private BucketRepository bucketRepository;

    @Condition
    public boolean shouldApply(
            @Fact("PUT") boolean isPut,
            @Fact("awsAccessKey") String awsAccessKey,
            @Fact("bucketName") String bucketName) {
        return verifyBucketName(bucketName) &&
                verifyBucketNotExist(bucketName);
    }

    @Action
    public void makeBucket(Facts facts,
                           @Fact("awsAccessKey") String awsAccessKey,
                           @Fact("bucketName") String bucketName) {
        BucketModel bucket = new BucketModel();
        bucket.setName(bucketName);
        bucket.setOwner(awsAccessKey);
        bucket.setCreatedDate(System.currentTimeMillis());
        bucketRepository.save(bucket);
        facts.put("statusCode", SC_OK);
    }

    private boolean verifyBucketNotExist(String name) {
        if (bucketRepository.findOne(name) != null) {
            throw new SpringStorageException(ErrorCode.BUCKET_ALREADY_EXISTS);
        }
        return true;
    }

    private boolean verifyBucketName(String name) {
        if (name == null) {
            throw new SpringStorageException(ErrorCode.INVALID_BUCKET_NAME);
        }

        // Bucket names cannot be no less than 3 and no more than 63 characters long.
        if (name.length() < 3 || name.length() > 63) {
            String message = "bucket name must be at least 3 and no more than 63 characters long";
            throw new SpringStorageException(ErrorCode.INVALID_BUCKET_NAME, message);
        }
        // Successive periods in bucket names are not allowed.
        if (name.matches("\\.\\.")) {
            String message = "bucket name cannot contain successive periods. For more information refer "
                    + "http://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html";
            throw new SpringStorageException(ErrorCode.INVALID_BUCKET_NAME, message);
        }
        // Bucket names should be dns compatible.
        if (!name.matches("^[a-z0-9][a-z0-9\\.\\-]+[a-z0-9]$")) {
            String message = "bucket name does not follow Amazon S3 standards. For more information refer "
                    + "http://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html";
            throw new SpringStorageException(ErrorCode.INVALID_BUCKET_NAME, message);
        }
        return true;
    }
}
