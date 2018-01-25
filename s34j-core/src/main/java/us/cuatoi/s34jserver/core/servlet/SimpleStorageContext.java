package us.cuatoi.s34jserver.core.servlet;

import us.cuatoi.s34jserver.core.StorageContext;

import java.nio.file.Path;

public class SimpleStorageContext implements StorageContext {
    private Path baseDir;
    private String accessKey;
    private String secretKey;
    private String serverId;

    /* Getters */
    @Override
    public Path getBaseDir() {
        return baseDir;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    @Override
    public String getServerId() {
        return serverId;
    }

    /* Setters */
    public SimpleStorageContext setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    public SimpleStorageContext setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public SimpleStorageContext setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public SimpleStorageContext setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    /* Others */
    @Override
    public String getSecretKey(String accessKey) {
        return secretKey;
    }
}
