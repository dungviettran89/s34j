package us.cuatoi.s34j.sbs.core.store.vfs;

import us.cuatoi.s34j.sbs.core.store.Store;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * An Apache VFS backed store.
 */
public class VfsStore implements Store {
    @Override
    public boolean has(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long size(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream load(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream save(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTotalBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getUsedBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAvailableBytes() {
        throw new UnsupportedOperationException();
    }
}
