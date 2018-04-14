package us.cuatoi.s34j.sbs.core;


import java.io.FileNotFoundException;
import java.io.InputStream;

public interface SimpleBlockStorage {

    long save(String key, InputStream input);

    InputStream load(String key) throws FileNotFoundException;
}
