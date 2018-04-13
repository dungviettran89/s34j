package us.cuatoi.s34j.sbs.core;

public interface SimpleBlockStorage {
    /**
     * Get total space of the store
     *
     * @return total bytes of the store
     */
    long getTotalBytes();

    /**
     * Get used space of the store
     *
     * @return used bytes of the store
     */
    long getUsedBytes();

    /**
     * Get available space of the store
     *
     * @return available bytes of the store
     */
    long getAvailableBytes();
}
