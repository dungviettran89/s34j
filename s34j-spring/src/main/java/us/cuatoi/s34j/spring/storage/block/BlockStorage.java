package us.cuatoi.s34j.spring.storage.block;

import java.io.IOException;
import java.io.InputStream;

public interface BlockStorage {
    long save(String key, InputStream input) throws IOException;

    InputStream load(String key) throws IOException;

    void delete(String key) throws IOException;
}
