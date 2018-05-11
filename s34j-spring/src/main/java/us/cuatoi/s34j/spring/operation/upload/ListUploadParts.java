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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.dto.InitiatorXml;
import us.cuatoi.s34j.spring.dto.ListPartsResultXml;
import us.cuatoi.s34j.spring.dto.OwnerXml;
import us.cuatoi.s34j.spring.dto.PartResponseXml;
import us.cuatoi.s34j.spring.model.UploadModel;
import us.cuatoi.s34j.spring.model.UploadPartModel;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;
import static us.cuatoi.s34j.spring.helper.StorageHelper.toResponseDateString;

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
    public void listUploadParts(Facts facts, @Fact("bucketName") String bucketName,
                                @Fact("objectName") String objectName, @Fact("query:uploadId") String uploadId) {
        UploadModel uploadModel = uploadRepository.findOne(uploadId);
        String maxPartsString = facts.get("query:max-parts");
        int maxParts = NumberUtils.toInt(maxPartsString, 1000);
        String partNumberMarkerString = facts.get("query:part-number-marker");
        int partNumberMarker = NumberUtils.toInt(partNumberMarkerString, 0);
        logger.info("listUploadParts() bucketName={} objectName={} uploadId={} maxPartsString={} partNumberMarkerString{}",
                bucketName, objectName, uploadId, maxPartsString, partNumberMarkerString);
        PageRequest page = new PageRequest(0, maxParts);
        Page<UploadPartModel> partPage = uploadPartRepository.findAllByUploadIdAndUploadPartOrderGreaterThanOrderByUploadPartOrder(uploadId,
                partNumberMarker, page);

        InitiatorXml initiator = new InitiatorXml();
        initiator.setId(uploadModel.getInitiator());
        initiator.setDisplayName(uploadModel.getInitiator());
        OwnerXml owner = new OwnerXml();
        owner.setId(uploadModel.getOwner());
        owner.setDisplayName(uploadModel.getOwner());
        ListPartsResultXml response = new ListPartsResultXml();
        response.setBucket(bucketName);
        response.setKey(objectName);
        response.setUploadId(uploadId);
        response.setMaxParts(maxParts);
        response.setOwner(owner);
        response.setInitiator(initiator);
        response.setTruncated(partPage.getTotalElements() > partPage.getSize());
        response.setStorageClass(SpringStorageConstants.STORAGE_CLASS);
        for (UploadPartModel partModel : partPage.getContent()) {
            PartResponseXml part = new PartResponseXml();
            part.seteTag(partModel.getEtag());
            part.setPartNumber(partModel.getUploadPartOrder());
            part.setSize(partModel.getSize());
            part.setLastModified(toResponseDateString(partModel.getCreatedDate()));
            response.getParts().add(part);
            response.setNextPartNumberMarker(String.valueOf(partModel.getUploadPartOrder()));
        }
        facts.put("statusCode", SC_OK);
        facts.put("response", response);
    }

}
