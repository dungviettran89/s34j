package us.cuatoi.s34j.spring.operation.bucket;

import org.jeasy.rules.annotation.*;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.dto.LocationConstraintXml;
import us.cuatoi.s34j.spring.model.BucketModel;
import us.cuatoi.s34j.spring.model.BucketRepository;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;
import static us.cuatoi.s34j.spring.SpringStorageConstants.CONTENT_TYPE;

@Service
@Rule(name = "GetBucketLocation")
public class GetBucketLocation extends AbstractBucketRule {
    public static final Logger logger = LoggerFactory.getLogger(GetBucketLocation.class);
    @Autowired
    private BucketRepository bucketRepository;

    @Priority
    public int priority() {
        return 100;
    }

    @Condition
    public boolean shouldApply(
            @Fact("GET") boolean isGet,
            @Fact("query:location") String location,
            @Fact("bucketName") String bucketName) {
        return isGet && isNotBlank(bucketName) && location != null;
    }


    @Action
    public void returnLocation(Facts facts,
                               @Fact("bucketName") String bucketName) {
        BucketModel model = bucketRepository.findOne(bucketName);
        LocationConstraintXml location = new LocationConstraintXml();
        location.setRegion(model.getLocation());

        facts.put("statusCode", SC_OK);
        facts.put("contentType", CONTENT_TYPE);
        facts.put("response", location);
    }


}
