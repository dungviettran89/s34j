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
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.model.UploadPartRepository;

import java.io.InputStream;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "PutUploadPart")
public class PutUploadPart extends AbstractUploadRule {
    public static final Logger logger = LoggerFactory.getLogger(PutUploadPart.class);

    @Autowired
    private UploadPartRepository uploadPartRepository;

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
        facts.put("errorCode", ErrorCode.NOT_IMPLEMENTED);
    }
}
