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

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.ObjectManager;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Rule(name = "DeleteObject")
public class DeleteObject extends AbstractObjectRule {

    @Autowired
    private ObjectManager objectManager;

    @Condition
    public boolean shouldApply(Facts facts,
                               @Fact("DELETE") boolean isDeleting,
                               @Fact("objectName") String objectName,
                               @Fact("bucketName") String bucketName) {
        return isDeleting && isNotBlank(objectName) && isNotBlank(bucketName) && isBlank(facts.get("query:uploadId"));
    }

    @Action(order = 10)
    public void perform(Facts facts,
                        @Fact("objectName") String objectName,
                        @Fact("bucketName") String bucketName) {
        objectManager.deleteCurrentVersionIfExists(objectName, bucketName);
        facts.put("statusCode", 200);
    }
}
