package us.cuatoi.s34j.spring.operation;

import org.jeasy.rules.annotation.*;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.dto.LocationConstraintXml;
import us.cuatoi.s34j.spring.model.BucketModel;
import us.cuatoi.s34j.spring.model.BucketRepository;

import static org.apache.http.HttpStatus.SC_OK;

@Service
@Rule(name = "GetBucketLocation")
public class GetBucketLocation implements ExecutionRule {
    public static final Logger logger = LoggerFactory.getLogger(GetBucketLocation.class);
    @Autowired
    private BucketRepository bucketRepository;

    @Priority
    public int priority() {
        return 10;
    }

    @Condition
    public boolean shouldApply(
            @Fact("GET") boolean isGet,
            @Fact("query:location") String location,
            @Fact("bucketName") String bucketName) {
        return true;
    }


    @Action
    public void returnLocation(Facts facts,
                               @Fact("bucketName") String bucketName) {
        BucketModel model = bucketRepository.findOne(bucketName);
        logger.info("returnLocation() model=" + model);
        if (model == null) {
            throw new SpringStorageException(ErrorCode.NO_SUCH_BUCKET);
        }
        LocationConstraintXml location = new LocationConstraintXml();
        location.setRegion(model.getLocation());

        facts.put("statusCode", SC_OK);
        facts.put("contentType", "application/xml; charset=utf-8");
        facts.put("response", location);
    }
}
