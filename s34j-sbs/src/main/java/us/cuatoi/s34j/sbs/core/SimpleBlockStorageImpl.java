package us.cuatoi.s34j.sbs.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.operation.BlockDeleter;
import us.cuatoi.s34j.sbs.core.operation.BlockLoader;
import us.cuatoi.s34j.sbs.core.operation.BlockSaver;

import java.io.FileNotFoundException;
import java.io.InputStream;

@Service
public class SimpleBlockStorageImpl implements SimpleBlockStorage {
    @Autowired
    private BlockSaver blockSaver;
    @Autowired
    private BlockLoader blockLoader;
    @Autowired
    private BlockDeleter blockDeleter;

    @Override
    public long save(String key, InputStream input) {
        return blockSaver.save(key, input);
    }

    @Override
    public InputStream load(String key) throws FileNotFoundException {
        return blockLoader.load(key);
    }

    @Override
    public void delete(String key) throws FileNotFoundException {
        blockDeleter.delete(key);
    }
}
