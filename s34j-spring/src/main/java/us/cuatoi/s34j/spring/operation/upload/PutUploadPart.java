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
import us.cuatoi.s34j.spring.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static us.cuatoi.s34j.spring.VersionHelper.newVersion;

@Service
@Rule(name = "PutUploadPart")
public class PutUploadPart extends AbstractUploadRule {
    public static final Logger logger = LoggerFactory.getLogger(PutUploadPart.class);

    @Autowired
    private UploadPartRepository uploadPartRepository;
    @Autowired
    private PartManager partManager;
    @Autowired
    private PartRepository partRepository;

    @Condition
    public boolean shouldApply(Facts facts, @Fact("PUT") boolean isPut,
                               @Fact("bucketName") String bucketName,
                               @Fact("objectName") String objectName,
                               @Fact("query:uploadId") String uploadId,
                               @Fact("parts") List<InputStream> parts) {
        return isPut &&
                isNotBlank(bucketName) &&
                isNotBlank(objectName) &&
                isNotBlank(uploadId) &&
                parts.size() > 0 &&
                facts.get("x-amz-copy-source") == null;
    }

    @Action
    public void putUploadPart(Facts facts, @Fact("bucketName") String bucketName,
                              @Fact("objectName") String objectName,
                              @Fact("query:partNumber") String partNumber,
                              @Fact("query:uploadId") String uploadId,
                              @Fact("parts") List<InputStream> parts) {
        //overrides old version
        UploadPartModel oldUploadPartVersion = uploadPartRepository.findOneByUploadPartOrderAndUploadId(partNumber, uploadId);
        if (oldUploadPartVersion != null) {
            List<PartModel> deletedOldParts = partRepository.findAllByUploadPartId(oldUploadPartVersion.getUploadPartId());
            partManager.deletePart(deletedOldParts);
            uploadPartRepository.delete(oldUploadPartVersion);
            logger.info("putUploadPart() oldUploadPartVersion=" + oldUploadPartVersion);
            logger.info("putUploadPart() deletedOldParts=" + deletedOldParts);
        }

        //save new version
        UploadPartModel uploadPartModel = new UploadPartModel();
        uploadPartModel.setUploadPartId(newVersion());
        uploadPartModel.setBucketName(bucketName);
        uploadPartModel.setObjectName(objectName);
        uploadPartModel.setUploadId(uploadId);
        uploadPartModel.setUploadPartOrder(partNumber);
        uploadPartModel.setCreatedDate(System.currentTimeMillis());
        uploadPartModel.setEtag(facts.get("ETag"));

        ArrayList<PartModel> newParts = new ArrayList<>();
        for (String partName : partManager.savePart(parts)) {
            PartModel model = new PartModel();
            model.setPartName(partName);
            model.setPartId(newVersion());
            model.setObjectName(objectName);
            model.setBucketName(bucketName);
            model.setUploadPartId(uploadPartModel.getUploadPartId());
            newParts.add(model);
        }

        partRepository.save(newParts);
        uploadPartRepository.save(uploadPartModel);
        facts.put("statusCode", 200);
        logger.info("putUploadPart() newParts=" + newParts);
        logger.info("putUploadPart() uploadPartModel=" + uploadPartModel);
    }
}
