package us.cuatoi.s34j.sbs.core.store.sardine;

import com.github.sardine.SardineFactory;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreProvider;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@ConditionalOnClass(SardineFactory.class)
public class SardineStoreProvider implements StoreProvider<SardineConfiguration> {

    public static final Logger logger = LoggerFactory.getLogger(SardineStoreProvider.class);


    @Override
    public String getType() {
        return "sardine";
    }

    @Override
    public Class<? extends SardineConfiguration> getConfigClass() {
        return SardineConfiguration.class;
    }

    @Override
    public Store createStore(String uriString, SardineConfiguration config) {
        logger.info("createStore() uriString=" + uriString);
        Preconditions.checkArgument(isNotBlank(uriString));
        logger.info("createStore() config=" + config);
        Preconditions.checkNotNull(config);
        return new SardineStore(uriString, config.getUser(), config.getPassword());
    }
}
