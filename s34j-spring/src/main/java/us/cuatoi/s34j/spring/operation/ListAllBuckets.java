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

package us.cuatoi.s34j.spring.operation;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.dto.BucketXml;
import us.cuatoi.s34j.spring.dto.BucketsXml;
import us.cuatoi.s34j.spring.dto.ListAllMyBucketsResultXml;
import us.cuatoi.s34j.spring.dto.OwnerXml;
import us.cuatoi.s34j.spring.helper.StorageHelper;
import us.cuatoi.s34j.spring.model.BucketRepository;

import java.util.List;
import java.util.stream.Collectors;

import static us.cuatoi.s34j.spring.SpringStorageConstants.CONTENT_TYPE;

@Service
@Rule(name = "ListAllBuckets")
public class ListAllBuckets implements ExecutionRule {
    @Autowired
    private BucketRepository bucketRepository;

    @Condition
    public boolean shouldRun(@Fact("GET") boolean isGet, @Fact("path") String path) {
        return isGet && StringUtils.equalsIgnoreCase(path, "/");
    }

    @Action
    public void returnBucketList(Facts facts, @Fact("awsAccessKey") String awsAccessKey) {
        List<BucketXml> bucketList = Lists.newArrayList(bucketRepository.findAll()).stream()
                .map((b) -> {
                    BucketXml xml = new BucketXml();
                    xml.setName(b.getBucketName());
                    xml.setCreationDate(StorageHelper.toResponseDateString(b.getCreatedDate()));
                    return xml;
                })
                .collect(Collectors.toList());
        BucketsXml buckets = new BucketsXml();
        buckets.setBucketList(bucketList);
        OwnerXml ownerXml = new OwnerXml();
        ownerXml.setId(awsAccessKey);
        ownerXml.setDisplayName(awsAccessKey);
        ListAllMyBucketsResultXml response = new ListAllMyBucketsResultXml();
        response.setOwner(ownerXml);
        response.setBuckets(buckets);
        facts.put("statusCode", 200);
        facts.put("contentType", CONTENT_TYPE);
        facts.put("response", response);
    }
}
