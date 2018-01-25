package us.cuatoi.s34jserver.core;

import java.nio.file.Path;

public abstract class S3Context {

    public static final int MAX_DIFFERENT_IN_REQUEST_TIME = Integer.parseInt(System.getProperty("s34j.auth.request.maxDifferenceMinutes", "15")) * 60 * 1000;
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

    public Path getBaseMetadataDir() {
        return basePath.resolve(S3Constants.WORK_DIR).resolve(S3Constants.METADATA_DIR);
    }

    public Path getBaseUploadDir() {
        return basePath.resolve(S3Constants.WORK_DIR).resolve(S3Constants.UPLOAD_DIR);
    }
}
