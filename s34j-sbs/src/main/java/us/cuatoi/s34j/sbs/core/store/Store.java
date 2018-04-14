package us.cuatoi.s34j.sbs.core.store;

import java.io.InputStream;

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
     *  @return input stream to read content of the block.
     *
     */
    InputStream load(String key);

    /**
     * Save the block as the form of input stream
     *  @param key    of the block
     * @param is the stream to save
     */
    void save(String key, InputStream is);

    /**
     * Delete the block if exists
     *
     * @param key of the block
     * @return true if block exists
     */
    boolean delete(String key);

    /**
     * Get available space of the store
     *
     * @param usedByte known used bytes
     * @return available bytes of the store
     */
    long getAvailableBytes(long usedByte);

}
