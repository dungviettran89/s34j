package us.cuatoi.s34j.sbs.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.operation.SaveBlockOperation;

import java.io.InputStream;

@Service
public class SimpleBlockStorageImpl implements SimpleBlockStorage {
    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public boolean has(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long size(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load(String key, FileConsumer consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(String key, InputStream stream) {
        SaveBlockOperation operation = new SaveBlockOperation(key, stream);
        beanFactory.autowireBean(operation);
        operation.execute();
    }

    @Override
    public boolean delete(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTotal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getUsed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAvailable() {
        throw new UnsupportedOperationException();
    }
}
