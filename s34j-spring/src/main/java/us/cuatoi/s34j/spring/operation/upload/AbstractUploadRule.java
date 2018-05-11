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
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;
import us.cuatoi.s34j.spring.model.UploadPartRepository;
import us.cuatoi.s34j.spring.model.UploadRepository;
import us.cuatoi.s34j.spring.operation.bucket.AbstractBucketRule;

public abstract class AbstractUploadRule extends AbstractBucketRule {
    @Autowired
    protected UploadRepository uploadRepository;
    @Autowired
    protected UploadPartRepository uploadPartRepository;

    @Action(order = -1)
    public void checkUploadExists(Facts facts, @Fact("query:uploadId") String uploadId) {
        if (uploadRepository.findOne(uploadId) == null) {
            facts.put("errorCode", ErrorCode.NO_SUCH_UPLOAD);
            throw new SpringStorageException(ErrorCode.NO_SUCH_UPLOAD);
        }
    }
}
