package us.cuatoi.s34j.spring.auth;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import org.jeasy.rules.annotation.*;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Rule(name = "ContentMD5Verifier")
public class ContentMD5Verifier implements AuthenticationRule {
    @Priority
    public int priority() {
        return 10;
    }

    @Condition
    public boolean shouldVerify(@Fact("header:content-md5") String contentMd5) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Action
    public void hashContent(Facts facts, @Fact("inputStream") InputStream inputStream) {
        HashingInputStream hashingStream = new HashingInputStream(Hashing.md5(), inputStream);
        facts.put("inputStream", hashingStream);
        facts.put("md5", hashingStream);
    }
}
