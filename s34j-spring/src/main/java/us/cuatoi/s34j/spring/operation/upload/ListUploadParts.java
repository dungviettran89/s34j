package us.cuatoi.s34j.spring.operation.upload;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.dto.ErrorCode;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "ListUploadParts")
public class ListUploadParts extends AbstractUploadRule {

    private static final Logger logger = LoggerFactory.getLogger(ListUploadParts.class);

    @Condition
    public boolean shouldApply(
            @Fact("GET") boolean isGet,
            @Fact("bucketName") String bucketName,
            @Fact("objectName") String objectName,
            @Fact("query:uploadId") String uploadId) {
        return isGet &&
                isNotBlank(bucketName) &&
                isNotBlank(objectName) &&
                isNotBlank(uploadId);
    }

    @Action
    public void listUploadParts(Facts facts, @Fact("query:uploadId") String uploadId) {
        facts.put("errorCode", ErrorCode.NOT_IMPLEMENTED);
    }
}
