package us.cuatoi.s34jserver.core;

import java.nio.file.Path;

public interface StorageContext {

    /* Getters */
    Path getBaseDir();

    String getServerId();

    /* Others */
    String getSecretKey(String accessKey);

    String getRegion();
}
