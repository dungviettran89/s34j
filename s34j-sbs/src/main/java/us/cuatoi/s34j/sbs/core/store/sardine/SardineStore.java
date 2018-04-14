package us.cuatoi.s34j.sbs.core.store.sardine;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.google.common.base.Preconditions;
import com.google.common.io.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.sbs.core.StoreHelper;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SardineStore implements Store {

    private static final Logger logger = LoggerFactory.getLogger(SardineStore.class);
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private final String url;
    private final String user;
    private final String password;

    public SardineStore(String url, String user, String password) {
        logger.info("SardineStore() url=" + url);
        Preconditions.checkArgument(isNotBlank(url));
        logger.info("SardineStore() user=" + user);
        Preconditions.checkArgument(isNotBlank(user));
        logger.info("SardineStore() password=" + password);
        Preconditions.checkArgument(isNotBlank(password));
        this.url = endsWith(url, "/") ? url : url + "/";
        this.user = user;
        this.password = password;
    }

    @Override
    public InputStream load(String key) {
        logger.info("load(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            return SardineFactory.begin(user, password).get(url + key);
        } catch (IOException exception) {
            logger.error("load(): exception=" + exception, exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public long save(String key, InputStream is) {
        logger.info("save(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            Sardine sardine = SardineFactory.begin(user, password);
            sardine.enablePreemptiveAuthentication(URI.create(url).getHost());
            CountingInputStream cis = new CountingInputStream(is);
            sardine.put(url + key, cis);
            return cis.getCount();
        } catch (Exception exception) {
            logger.error("save(): exception=" + exception, exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public boolean delete(String key) {
        logger.info("delete(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            SardineFactory.begin(user, password).delete(url + key);
            return true;
        } catch (IOException exception) {
            logger.error("delete(): getAvailableBytes=" + exception, exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public long getAvailableBytes(long usedByte) {
        try {
            long availableBytes = SardineFactory.begin(user, password).getQuota(url).getQuotaAvailableBytes();
            logger.info("getAvailableBytes(): availableBytes=" + availableBytes);
            return availableBytes;
        } catch (IOException exception) {
            logger.error("getAvailableBytes(): getAvailableBytes=" + exception, exception);
            throw new StoreException(exception);
        }
    }
}
