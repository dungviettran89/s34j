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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.dto.*;
import us.cuatoi.s34j.spring.helper.StorageHelper;
import us.cuatoi.s34j.spring.model.UploadModel;
import us.cuatoi.s34j.spring.model.UploadRepository;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

import java.util.ArrayList;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "ListUploads")
public class ListUploads extends AbstractBucketRule {

    public static final Logger logger = LoggerFactory.getLogger(ListUploads.class);
    @Autowired
    private UploadRepository uploadRepository;

    @Condition
    public boolean shouldApply(
            @Fact("GET") boolean isGet,
            @Fact("bucketName") String bucketName,
            @Fact("query:uploads") String uploads) {
        return isGet && isNotBlank(bucketName) && uploads != null;
    }

    @Action
    public void listUploads(Facts facts) {
        String bucketName = facts.get("bucketName");
        String delimiter = facts.get("query:delimiter");
        String prefix = facts.get("query:prefix");
        String keyMarker = facts.get("query:key-marker");
        String uploadIdMarker = facts.get("query:upload-id-marker");
        int maxUploads = NumberUtils.toInt(facts.get("query:max-uploads"), 1000);

        ArrayList<UploadXml> uploadXmls = new ArrayList<>();
        ArrayList<PrefixXml> commonPrefixes = new ArrayList<>();
        boolean truncated;
        String nextKeyMarker = null;
        String nextUploadIdMarker = null;
        if (isBlank(delimiter)) {
            Page<UploadModel> uploads = uploadRepository.findByBucketNameAndObjectNamePrefixStartsWithAndObjectNameGreaterThanAndUploadIdGreaterThanOrderByObjectName(
                    bucketName, prefix, keyMarker, uploadIdMarker, new PageRequest(0, maxUploads));
            truncated = uploads.getTotalElements() > uploads.getSize();
            UploadModel lastUpload = StreamSupport.stream(uploads.spliterator(), false)
                    .reduce((u1, u2) -> u2).orElse(null);
            if (truncated && lastUpload != null) {
                nextKeyMarker = lastUpload.getObjectName();
                nextUploadIdMarker = lastUpload.getUploadId();
            }
            uploads.forEach((m) -> {
                OwnerXml owner = new OwnerXml();
                owner.setId(m.getOwner());
                owner.setDisplayName(m.getOwner());

                InitiatorXml initiator = new InitiatorXml();
                initiator.setId(m.getInitiator());
                initiator.setDisplayName(m.getInitiator());

                UploadXml xml = new UploadXml();
                xml.setObjectName(m.getObjectName());
                xml.setStorageClass(SpringStorageConstants.STORAGE_CLASS);
                xml.setInitiated(StorageHelper.toResponseDateString(m.getCreatedDate()));
                xml.setUploadId(m.getUploadId());
                xml.setOwner(owner);
                xml.setInitiator(initiator);
                uploadXmls.add(xml);
            });
        } else {
            facts.put("errorCode", ErrorCode.NOT_IMPLEMENTED);
            return;
        }

        ListMultipartUploadsResultXml response = new ListMultipartUploadsResultXml();
        response.setBucketName(bucketName);
        response.setUploadIdMarker(uploadIdMarker);
        response.setKeyMarker(keyMarker);
        response.setMaxUploads(maxUploads);
        response.setUploads(uploadXmls);
        response.setCommonPrefixes(commonPrefixes);
        response.setTruncated(truncated);
        response.setNextKeyMarker(nextKeyMarker);
        response.setNextUploadIdMarker(nextUploadIdMarker);
        facts.put("statusCode", 200);
        facts.put("response", response);
        logger.info("listUploads() bucketName=" + bucketName);
        logger.info("listUploads() uploadIdMarker=" + uploadIdMarker);
        logger.info("listUploads() keyMarker=" + keyMarker);
        logger.info("listUploads() prefix=" + prefix);
        logger.info("listUploads() maxUploads=" + maxUploads);
        logger.info("listUploads() response=" + response);
    }
}
