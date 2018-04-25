package us.cuatoi.s34j.spring.operation.upload;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.model.UploadRepository;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

public abstract class AbstractUploadRule extends AbstractBucketRule {
    @Autowired
    protected UploadRepository uploadRepository;

    @Action(order = -1)
    public void checkUploadExists(Facts facts, @Fact("query:uploadId") String uploadId) {
        if (uploadRepository.findOne(uploadId) == null) {
            facts.put("errorCode", ErrorCode.NO_SUCH_UPLOAD);
            throw new SpringStorageException(ErrorCode.NO_SUCH_UPLOAD);
        }
    }
}
