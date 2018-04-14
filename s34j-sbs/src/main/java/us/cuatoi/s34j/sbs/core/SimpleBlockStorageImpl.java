package us.cuatoi.s34j.sbs.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

@Service
public class SimpleBlockStorageImpl implements SimpleBlockStorage {
    @Autowired
    private AutowireCapableBeanFactory beanFactory;

}
