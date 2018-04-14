package us.cuatoi.s34j.sbs.core.store;

import java.io.InputStream;

/**
 * Store interface to save block into external store, which can be nio based or webdav based.
 */
public interface Store {

    /**
     * Load the block to an input stream
     *  @param key      of the block
     *  @return input stream to read content of the block.
     *
     */
    InputStream load(String key);

    /**
     * Save the block as the form of input stream
     * @param key    of the block
     * @param is the stream to save
     * @return byte counts
     */
    long save(String key, InputStream is);

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
