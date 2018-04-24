package us.cuatoi.s34j.spring.operation.upload;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.dto.InitiateMultipartUploadResultXml;
import us.cuatoi.s34j.spring.helper.DateHelper;
import us.cuatoi.s34j.spring.model.BucketModel;
import us.cuatoi.s34j.spring.model.BucketRepository;
import us.cuatoi.s34j.spring.model.UploadModel;
import us.cuatoi.s34j.spring.model.UploadRepository;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

import java.util.Date;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "InitiateUpload")
public class InitiateUpload extends AbstractBucketRule {
    public static final Logger logger = LoggerFactory.getLogger(InitiateUpload.class);
    @Autowired
    private UploadRepository uploadRepository;
    @Autowired
    private BucketRepository bucketRepository;

    @Condition
    public boolean shouldApply(@Fact("POST") boolean isPost, @Fact("bucketName") String bucketName,
                               @Fact("objectName") String objectName, @Fact("query:uploads") String uploads) {
        return isPost && isNotBlank(bucketName) && isNotBlank(objectName) && uploads != null;
    }

    @Action
    public void apply(Facts facts, @Fact("bucketName") String bucketName,
                      @Fact("objectName") String objectName, @Fact("awsAccessKey") String awsAccessKey) {
        String uploadId = DateHelper.format(SpringStorageConstants.X_AMZ_DATE_FORMAT, new Date()) +
                "-" + UUID.randomUUID().toString();
        BucketModel bucket = bucketRepository.findOne(bucketName);
        String owner = bucket.getOwner();

        UploadModel model = new UploadModel();
        model.setUploadId(uploadId);
        model.setBucketName(bucketName);
        model.setObjectName(objectName);
        model.setOwner(owner);
        model.setInitiator(awsAccessKey);
        model.setCreatedDate(System.currentTimeMillis());
        uploadRepository.save(model);
        InitiateMultipartUploadResultXml response = new InitiateMultipartUploadResultXml();
        response.setBucket(bucketName);
        response.setKey(objectName);
        response.setUploadId(uploadId);
        facts.put("statusCode", 200);
        facts.put("response", response);
        logger.info("apply() bucketName=" + bucketName);
        logger.info("apply() objectName=" + objectName);
        logger.info("apply() uploadId=" + uploadId);
        logger.info("apply() model=" + model);
        logger.info("apply() response=" + response);
    }

}
