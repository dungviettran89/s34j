package us.cuatoi.s34j.spring.auth;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import org.jeasy.rules.annotation.*;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Service;

import java.io.InputStream;

import static org.apache.commons.lang3.StringUtils.equalsAny;

@Service
@Rule(name = "ContentSHA256Verifier")
public class ContentSHA256Verifier implements AuthenticationRule {
    @Priority
    public int priority() {
        return 10;
    }

    @Condition
    public boolean shouldVerify(@Fact("header:x-amz-content-sha256") String contentSha256) {
        return !equalsAny(contentSha256, "STREAMING-AWS4-HMAC-SHA256-PAYLOAD", "UNSIGNED-PAYLOAD");
    }

    @Action
    public void hashContent(Facts facts, @Fact("inputStream") InputStream inputStream) {
        HashingInputStream hashingStream = new HashingInputStream(Hashing.sha256(), inputStream);
        facts.put("inputStream", hashingStream);
        facts.put("sha256", hashingStream);
    }
}
