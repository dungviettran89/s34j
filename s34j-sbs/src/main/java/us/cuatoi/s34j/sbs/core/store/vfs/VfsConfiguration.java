package us.cuatoi.s34j.sbs.core.store.vfs;

import java.io.Serializable;

/**
 * Information needed to init a new Vfs store
 */
public class VfsConfiguration implements Serializable {

    private long totalBytes;

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }
}
