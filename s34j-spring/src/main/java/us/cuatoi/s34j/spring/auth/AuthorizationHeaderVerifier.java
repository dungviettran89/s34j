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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Rule(name = "AuthorizationHeaderVerifier")
public class AuthorizationHeaderVerifier extends AbstractVerifier implements AuthenticationRule {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationHeaderVerifier.class);

    @Priority
    public int priority() {
        return 10;
    }

    @Condition
    public boolean verifyHeader(Facts facts,
                                @Fact("method") String method,
                                @Fact("path") String path,
                                @Fact("header:authorization") String authorization,
                                @Fact("header:x-amz-date") String xAmzDate,
                                @Fact("header:x-amz-content-sha256") String xAmzContentSha256) {
        return verifyAuthorizationHeader(facts);
    }

    @Action
    public void verify(Facts facts) {
        facts.put("authenticated", true);
    }
}
