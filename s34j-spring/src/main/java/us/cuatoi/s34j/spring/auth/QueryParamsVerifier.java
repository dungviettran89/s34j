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
import us.cuatoi.s34j.spring.SpringStorageException;
import us.cuatoi.s34j.spring.dto.ErrorCode;

@Service
@Rule(name = "QueryParamsVerifier")
public class QueryParamsVerifier implements AuthenticationRule {

    @Priority
    public int priority() {
        return 1;
    }

    @Condition
    public boolean shouldVerify(
            @Fact("query:X-Amz-Signature") String signature
    ) {
        throw new SpringStorageException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Action
    public void verify(Facts facts) {

    }
}
