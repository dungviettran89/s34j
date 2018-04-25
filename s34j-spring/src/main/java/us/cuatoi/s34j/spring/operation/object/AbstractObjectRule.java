package us.cuatoi.s34j.spring.operation.object;

import com.google.gson.Gson;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.helper.Headers;
import us.cuatoi.s34j.spring.helper.StorageHelper;
import us.cuatoi.s34j.spring.model.ObjectModel;
import us.cuatoi.s34j.spring.model.ObjectRepository;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

import java.util.Date;

public abstract class AbstractObjectRule extends AbstractBucketRule {
    private static final Logger logger = LoggerFactory.getLogger(AbstractObjectRule.class);
    @Autowired
    private ObjectRepository objectRepository;

    @Action(order = -1)
    public void verifyObjectExists(Facts facts,
                                   @Fact("objectName") String objectName,
                                   @Fact("bucketName") String bucketName) {
        if (objectRepository.findOneByObjectNameAndBucketName(objectName, bucketName) == null) {
            logger.warn("verifyBucketExists() bucketName=" + bucketName);
            logger.warn("verifyBucketExists() objectName=" + objectName);
            logger.warn("verifyBucketExists() errorCode=" + ErrorCode.NO_SUCH_KEY);
            facts.put("errorCode", ErrorCode.NO_SUCH_KEY);
            throw new SpringStorageException(ErrorCode.NO_SUCH_KEY);
        }
    }

    protected void fillResponseHeaders(Facts facts, ObjectModel objectModel) {
        facts.put("responseHeader:contentLength", objectModel.getLength());
        facts.put("responseHeader:Last-Modified",
                StorageHelper.format(SpringStorageConstants.HTTP_HEADER_DATE_FORMAT, new Date(objectModel.getCreatedDate())));
        facts.put("responseHeaders", new Gson().fromJson(objectModel.getHeadersJson(), Headers.class));
    }
}
