package us.cuatoi.s34j.sbs.core.store;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Store interface to save block into external store, which can be nio based or webdav based.
 */
public abstract class Store {

    protected void validateKey(String key) {
        String message = "Invalid key, key must not be blank and only contains alphanumeric and . characters.";
        Preconditions.checkArgument(StringUtils.isNotBlank(key), message);
        Preconditions.checkArgument(key.matches("[a-zA-Z0-9.]+"), message);
    }

    /**
     * Check if the store has a particular key
     *
     * @param key to check
     * @return true if the key is available
     */
    public abstract boolean has(String key);

    /**
     * Check the size of the block
     *
     * @param key of the block
     * @return size of the block.
     */
    public abstract long size(String key);

    /**
     * Load the block to an input stream
     *
     * @param key      of the block
     * @param consumer to read the content
     */
    public abstract void load(String key, FileConsumer consumer);

    /**
     * Save the block as the form of input stream
     *
     * @param key    of the block
     * @param stream content of the block
     */
    public abstract void save(String key, InputStream stream);

    /**
     * Delete the block if exists
     *
     * @param key of the block
     * @return true if block exists
     */
    public abstract boolean delete(String key);

    /**
     * Get total space of the store
     *
     * @return total bytes of the store
     */
    public abstract long getTotal();

    /**
     * Get used space of the store
     *
     * @return used bytes of the store
     */
    public abstract long getUsed();

    /**
     * Get available space of the store
     *
     * @return available bytes of the store
     */
    public abstract long getAvailable();

    public interface FileConsumer {
        void accept(InputStream is) throws IOException;
    }
}
