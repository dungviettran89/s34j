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

package us.cuatoi.s34j.spring.auth;

import org.jeasy.rules.annotation.*;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Service
@Rule(name = "ChunkedUploadVerifier")
public class ChunkedUploadVerifier implements AuthenticationRule {

    @Priority
    public int priority() {
        return 1;
    }

    @Condition
    public boolean shouldVerify(Facts facts,
                                @Fact("header:content-encoding") String contentEncoding,
                                @Fact("header:x-amz-content-sha256") String xAmzContentSha256,
                                @Fact("header:authorization") String authorization
    ) {
        boolean isMultipleChunk = contains(contentEncoding, "aws-chunked") &&
                equalsIgnoreCase(xAmzContentSha256, SpringStorageConstants.STREAMING_PAYLOAD);
        if (!isMultipleChunk) {
            return false;
        }
        throw new SpringStorageException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Action
    public void verify(Facts facts) {
        facts.put("authenticated", true);
    }
}
