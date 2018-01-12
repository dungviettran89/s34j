package us.cuatoi.s34jserver.core;

import java.nio.file.Path;
import java.nio.file.Paths;

public class S3Context {

    private Path basePath = Paths.get("data");
    private String region = "us-central-1";
    private String serverId = "s34j";

    public String getServerId() {
        return serverId;
    }

    public String getSecretKey(String accessKey) {
        return "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG";
    }

    public Path getBasePath() {
        return basePath;
    }

    public String getRegion() {
        return region;
    }

    public S3Context setBasePath(Path basePath) {
        this.basePath = basePath;
        return this;
    }

    public S3Context setRegion(String region) {
        this.region = region;
        return this;
    }

    public S3Context setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }
}
