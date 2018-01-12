package us.cuatoi.s34jserver.core;

import java.nio.file.Path;

public abstract class S3Context {

    private final Path basePath;
    private String region = "us-central-1";
    private String serverId = "s34j";

    protected S3Context(Path basePath) {
        this.basePath = basePath;
    }

    public String getServerId() {
        return serverId;
    }

    public  abstract String getSecretKey(String accessKey);

    public Path getBasePath() {
        return basePath;
    }

    public String getRegion() {
        return region;
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
