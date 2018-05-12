/*
 * Copyright (C) 2018 dungviettran89@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package us.cuatoi.s34j.spring.operation.upload;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.PartModel;
import us.cuatoi.s34j.spring.model.UploadPartModel;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "DeleteUpload")
public class DeleteUpload extends AbstractUploadRule {

    public static final Logger logger = LoggerFactory.getLogger(DeleteUpload.class);

    @Condition
    public boolean shouldApply(@Fact("DELETE") boolean isDelete, @Fact("bucketName") String bucketName,
                               @Fact("objectName") String objectName, @Fact("query:uploadId") String uploadId) {
        return isDelete && isNotBlank(bucketName) && isNotBlank(objectName) && isNotBlank(uploadId);
    }

    @Action
    public void deleteUpload(Facts facts, @Fact("bucketName") String bucketName, @Fact("objectName") String objectName,
                             @Fact("query:uploadId") String uploadId) {
        long deletedPartCount = 0;
        List<UploadPartModel> uploadParts = uploadPartRepository.findAllByUploadId(uploadId);
        for (UploadPartModel uploadPart : uploadParts) {
            List<PartModel> deletedOldParts = partRepository.findAllByUploadPartIdOrderByPartOrder(uploadPart.getUploadPartId());
            partManager.deletePart(deletedOldParts);
            uploadPartRepository.delete(uploadPart);
            deletedPartCount += deletedOldParts.size();
        }
        uploadRepository.delete(uploadId);
        logger.info("deleteUpload() bucketName={} objectName={} uploadId={} deletedPartCount={}",
                bucketName, objectName, uploadId, deletedPartCount);
        facts.put("statusCode", 204);
    }
}
