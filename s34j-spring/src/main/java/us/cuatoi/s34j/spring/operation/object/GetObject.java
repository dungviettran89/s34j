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

package us.cuatoi.s34j.spring.operation.object;

import org.apache.commons.lang3.StringUtils;
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
import us.cuatoi.s34j.spring.helper.JoinInputStream;
import us.cuatoi.s34j.spring.helper.RangeInputStream;
import us.cuatoi.s34j.spring.model.ObjectModel;
import us.cuatoi.s34j.spring.model.ObjectRepository;
import us.cuatoi.s34j.spring.model.PartModel;
import us.cuatoi.s34j.spring.model.PartRepository;
import us.cuatoi.s34j.spring.storage.block.BlockStorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "GetObject")
public class GetObject extends AbstractObjectRule {

    private static final Logger logger = LoggerFactory.getLogger(GetObject.class);
    @Autowired
    private ObjectRepository objectRepository;
    @Autowired
    private BlockStorage blockStorage;
    @Autowired
    private PartRepository partRepository;

    @Condition
    public boolean shouldApply(Facts facts,
                               @Fact("GET") boolean isGet,
                               @Fact("objectName") String objectName,
                               @Fact("bucketName") String bucketName) {
        return isGet &&
                isNotBlank(objectName) &&
                isNotBlank(bucketName) &&
                facts.get("query:uploadId") == null;
    }

    @Action
    public void getObject(Facts facts, @Fact("objectName") String objectName, @Fact("bucketName") String bucketName) {
        facts.put("statusCode", 200);
        ObjectModel objectModel = objectRepository.findOneByObjectNameAndBucketName(objectName, bucketName);
        fillResponseHeaders(facts, objectModel);

        try {
            List<PartModel> parts = partRepository.findAllByObjectVersionOrderByPartOrder(objectModel.getObjectVersion());
            List<Callable<InputStream>> streams = parts.stream()
                    .map((p) -> (Callable<InputStream>) () -> blockStorage.load(p.getPartName()))
                    .collect(Collectors.toList());
            InputStream is = new JoinInputStream(streams);
            if (StringUtils.isNotBlank(facts.get("header:range"))) {
                is = new RangeInputStream(is, objectModel.getLength(), facts.get("header:range"));
            }

            facts.put("response", is);
            facts.put("statusCode", 200);
        } catch (IOException responseError) {
            logger.error("getObject() objectModel=" + objectModel);
            logger.error("getObject() responseError=" + responseError, responseError);
            facts.put("errorCode", ErrorCode.INTERNAL_ERROR);
        }
    }

}
