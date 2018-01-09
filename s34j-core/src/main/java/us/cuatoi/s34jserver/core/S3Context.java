package us.cuatoi.s34jserver.core;

import java.nio.file.Path;
import java.nio.file.Paths;

public class S3Context {
    public String getServerId() {
        return "s34j";
    }

    public String getSecretKey(String accessKey) {
        return "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG";
    }

    public Path getPath() {
        return Paths.get("data");
    }
}
