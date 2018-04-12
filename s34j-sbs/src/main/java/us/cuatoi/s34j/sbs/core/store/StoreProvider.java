package us.cuatoi.s34j.sbs.core.store;

import java.io.Serializable;

public interface StoreProvider<C extends Serializable> {
    String getType();

    Class<? extends C> getConfigClass();

    Store createStore(String uri, C config);
}
