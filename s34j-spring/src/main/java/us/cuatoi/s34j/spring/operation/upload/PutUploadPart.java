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

import org.apache.commons.lang3.math.NumberUtils;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.PartModel;
import us.cuatoi.s34j.spring.model.PartRepository;
import us.cuatoi.s34j.spring.model.UploadPartModel;
import us.cuatoi.s34j.spring.model.UploadPartRepository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static us.cuatoi.s34j.spring.helper.StorageHelper.newVersion;

@Service
@Rule(name = "PutUploadPart")
public class PutUploadPart extends AbstractUploadRule {
    public static final Logger logger = LoggerFactory.getLogger(PutUploadPart.class);

    @Autowired
    private UploadPartRepository uploadPartRepository;

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
                              @Fact("query:partNumber") String partNumberString,
                              @Fact("query:uploadId") String uploadId,
                              @Fact("parts") List<InputStream> parts) {
        //overrides old version
        long partNumber = NumberUtils.toLong(partNumberString);
        UploadPartModel oldUploadPartVersion = uploadPartRepository.findOneByUploadPartOrderAndUploadId(partNumber, uploadId);
        if (oldUploadPartVersion != null) {
            List<PartModel> deletedOldParts = partRepository.findAllByUploadPartIdOrderByPartOrder(oldUploadPartVersion.getUploadPartId());
            partManager.deletePart(deletedOldParts);
            uploadPartRepository.delete(oldUploadPartVersion);
            logger.info("putUploadPart() oldUploadPartVersion=" + oldUploadPartVersion);
            logger.info("putUploadPart() deletedOldParts=" + deletedOldParts);
        }

        String newPartId = newVersion();
        long size = 0;
        ArrayList<PartModel> newParts = new ArrayList<>();
        for (PartModel model : partManager.savePart(parts)) {
            model.setPartId(newVersion());
            model.setObjectName(objectName);
            model.setBucketName(bucketName);
            model.setUploadPartId(newPartId);
            newParts.add(model);
            size += model.getLength();
        }

        //save new version
        UploadPartModel uploadPartModel = new UploadPartModel();
        uploadPartModel.setUploadPartId(newPartId);
        uploadPartModel.setBucketName(bucketName);
        uploadPartModel.setObjectName(objectName);
        uploadPartModel.setUploadId(uploadId);
        uploadPartModel.setUploadPartOrder(partNumber);
        uploadPartModel.setCreatedDate(System.currentTimeMillis());
        uploadPartModel.setEtag(facts.get("ETag"));
        uploadPartModel.setSize(size);

        partRepository.save(newParts);
        uploadPartRepository.save(uploadPartModel);
        facts.put("statusCode", 200);
        logger.info("putUploadPart() newParts=" + newParts);
        logger.info("putUploadPart() uploadPartModel=" + uploadPartModel);
    }
}
