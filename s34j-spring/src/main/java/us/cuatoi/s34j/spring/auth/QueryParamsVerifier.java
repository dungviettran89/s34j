package us.cuatoi.s34j.spring.auth;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.jeasy.rules.annotation.*;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Service;

@Service
@Rule(name = "QueryParamsVerifier")
public class QueryParamsVerifier implements AuthenticationRule {

    @Priority
    public int priority() {
        return 1;
    }

    @Condition
    public boolean shouldVerify(
            @Fact("request") HttpServletRequest request,
            @Fact("query:X-Amz-Signature") String signature
    ) {
        return false;
    }

    @Action
    public void verify(Facts facts) {

    }
}
