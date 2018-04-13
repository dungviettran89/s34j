package us.cuatoi.s34j.sbs.core.store;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Store interface to save block into external store, which can be nio based or webdav based.
 */
public interface Store {

    /**
     * Check if the store has a particular key
     *
     * @param key to check
     * @return true if the key is available
     */
    boolean has(String key);

    /**
     * Check the size of the block
     *
     * @param key of the block
     * @return size of the block.
     */
    long size(String key);

    /**
     * Load the block to an input stream
     *  @param key      of the block
     *
     */
    InputStream load(String key);

    /**
     * Save the block as the form of input stream
     *  @param key    of the block
     *
     */
    OutputStream save(String key);

    /**
     * Delete the block if exists
     *
     * @param key of the block
     * @return true if block exists
     */
    boolean delete(String key);

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
