package us.cuatoi.s34j.sbs.core;

import com.google.gson.Gson;

import java.io.Serializable;

public class StoreStatus implements Serializable {
    private long usedBytes;
    private long availableBytes;
    private long keyCount;
    private long blockCount;
    private long activeStoreCount;
    private long configuredStoreCount;

    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(long usedBytes) {
        this.usedBytes = usedBytes;
    }

    public long getAvailableBytes() {
        return availableBytes;
    }

    public void setAvailableBytes(long availableBytes) {
        this.availableBytes = availableBytes;
    }

    public long getKeyCount() {
        return keyCount;
    }

    public void setKeyCount(long keyCount) {
        this.keyCount = keyCount;
    }

    public long getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(long blockCount) {
        this.blockCount = blockCount;
    }

    public long getActiveStoreCount() {
        return activeStoreCount;
    }

    public void setActiveStoreCount(long activeStoreCount) {
        this.activeStoreCount = activeStoreCount;
    }

    public long getConfiguredStoreCount() {
        return configuredStoreCount;
    }

    public void setConfiguredStoreCount(long configuredStoreCount) {
        this.configuredStoreCount = configuredStoreCount;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
