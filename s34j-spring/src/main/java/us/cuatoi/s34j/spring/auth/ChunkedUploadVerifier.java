package us.cuatoi.s34j.spring.auth;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.jeasy.rules.annotation.*;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Service;

@Service
@Rule(name = "ChunkedUploadVerifier")
public class ChunkedUploadVerifier implements AuthenticationRule {

    @Priority
    public int priority() {
        return 1;
    }

    @Condition
    public boolean shouldVerify(
            @Fact("request") HttpServletRequest request,
            @Fact("header:content-encoding") String contentEncoding,
            @Fact("header:x-amz-content-sha256") String contentSha256,
            @Fact("header:authorization") String authorization
    ) {
        return false;
    }

    @Action
    public void verify(Facts facts) {

    }
}
