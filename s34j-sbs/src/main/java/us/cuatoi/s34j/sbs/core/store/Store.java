package us.cuatoi.s34j.sbs.core.store;

import java.io.IOException;
import java.io.InputStream;

public interface Store {
    boolean has(String key);
    void save(String key, InputStream is) throws IOException;
    InputStream load(String key) throws IOException;
    boolean delete(String key) throws IOException;
    long getTotal() throws IOException;
    long getUsed() throws IOException;
    long getAvailable() throws IOException;
}
