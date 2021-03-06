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
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.CompleteMultipartUploadResultXml;
import us.cuatoi.s34j.spring.dto.CompleteMultipartUploadXml;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.dto.PartXml;
import us.cuatoi.s34j.spring.helper.StorageHelper;
import us.cuatoi.s34j.spring.model.ObjectModel;
import us.cuatoi.s34j.spring.model.PartModel;
import us.cuatoi.s34j.spring.model.UploadModel;
import us.cuatoi.s34j.spring.model.UploadPartModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "CompleteUpload")
public class CompleteUpload extends AbstractUploadRule {
    public static final Logger logger = LoggerFactory.getLogger(InitiateUpload.class);

    @Condition
    public boolean shouldApply(@Fact("POST") boolean isPost, @Fact("bucketName") String bucketName,
                               @Fact("objectName") String objectName, @Fact("query:uploadId") String uploadId) {
        return isPost && isNotBlank(bucketName) && isNotBlank(objectName) && isNotBlank(uploadId);
    }

    @Action
    public void completeUpload(Facts facts, @Fact("bucketName") String bucketName,
                               @Fact("objectName") String objectName, @Fact("query:uploadId") String uploadId,
                               @Fact("parts") List<InputStream> inputStreams) {
        try {
            UploadModel uploadModel = uploadRepository.findOne(uploadId);
            CompleteMultipartUploadXml requestXml = StorageHelper.parseXml(inputStreams, new CompleteMultipartUploadXml());
            List<UploadPartModel> uploadPartModels = new ArrayList<>();
            List<PartModel> parts = new ArrayList<>();
            long lastPartNumber = -1;
            for (PartXml partXml : requestXml.getParts()) {
                long currentPartNumber = NumberUtils.toLong(partXml.getPartNumber());
                if (lastPartNumber > currentPartNumber) {
                    throw new SpringStorageException(ErrorCode.INVALID_PART_ORDER);
                }
                UploadPartModel uploadPart = uploadPartRepository.findOneByUploadPartOrderAndUploadId(currentPartNumber, uploadId);
                if (uploadPart == null || !equalsIgnoreCase(partXml.geteTag(), uploadPart.getEtag())) {
                    throw new SpringStorageException(ErrorCode.INVALID_PART);
                }
                uploadPartModels.add(uploadPart);
                List<PartModel> partModels = partRepository.findAllByUploadPartIdOrderByPartOrder(uploadPart.getUploadPartId());
                if (partModels.size() == 0) {
                    throw new SpringStorageException(ErrorCode.INVALID_PART);
                }
                parts.addAll(partModels);
                lastPartNumber = currentPartNumber;
            }
            objectManager.deleteCurrentVersionIfExists(objectName, bucketName);

            int i = 0;
            long length = 0;
            for (PartModel part : parts) {
                part.setObjectVersion(uploadId);
                part.setUploadPartId(null);
                part.setPartOrder(i++);
                length += part.getLength();
            }
            partRepository.save(parts);

            ObjectModel objectModel = new ObjectModel();
            objectModel.setCreatedDate(System.currentTimeMillis());
            objectModel.setObjectVersion(uploadId);
            objectModel.setObjectName(objectName);
            objectModel.setBucketName(bucketName);
            objectModel.setLength(length);
            objectModel.setHeadersJson(uploadModel.getHeadersJson());
            objectRepository.save(objectModel);
            uploadRepository.delete(uploadModel);
            uploadPartRepository.delete(uploadPartModels);
            CompleteMultipartUploadResultXml responseXml = new CompleteMultipartUploadResultXml();
            responseXml.setBucket(bucketName);
            responseXml.setKey(objectName);
            responseXml.setLocation("/" + bucketName + "/" + objectName);
            responseXml.seteTag(objectModel.getObjectVersion());
            facts.put("statusCode", 200);
            facts.put("response", responseXml);
            facts.put("ETag", objectModel.getObjectVersion());
            logger.info("completeUpload() objectModel=" + objectModel);
            logger.info("completeUpload() parts=" + parts);
            logger.info("completeUpload() uploadModel=" + uploadModel);
            logger.info("completeUpload() uploadPartModels=" + uploadPartModels);
            logger.info("completeUpload() requestXml=" + requestXml);
        } catch (SpringStorageException knownError) {
            facts.put("errorCode", knownError.getErrorCode());
        } catch (Exception unexpectedError) {
            logger.error("completeUpload() bucketName=" + bucketName);
            logger.error("completeUpload() objectName=" + objectName);
            logger.error("completeUpload() uploadId=" + uploadId);
            logger.error("completeUpload() unexpectedError=" + unexpectedError, unexpectedError);
        }
    }
}
