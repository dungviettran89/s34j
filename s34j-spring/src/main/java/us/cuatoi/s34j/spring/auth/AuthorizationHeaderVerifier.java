package us.cuatoi.s34j.spring.auth;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Service;

@Service
@Rule(name = "AuthorizationHeaderVerifier")
public class AuthorizationHeaderVerifier implements AuthenticationRule {
    @Condition
    public boolean shouldVerify(
            @Fact("request") HttpServletRequest request,
            @Fact("header:authorization") String authorization,
            @Fact("header:x-amz-content-sha256") String contentSha256
    ) {
        return false;
    }

    @Action
    public void verify(Facts facts) {

    }
}
