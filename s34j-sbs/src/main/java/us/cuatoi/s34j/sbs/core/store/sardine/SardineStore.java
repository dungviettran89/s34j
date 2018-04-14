package us.cuatoi.s34j.sbs.core.store.sardine;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.sbs.core.StoreHelper;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreException;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        try {
            Sardine sardine = SardineFactory.begin(user, password);
            boolean baseFolderExists = sardine.exists(url);
            logger.info("SardineStore() baseFolderExists=" + baseFolderExists);
            if (!baseFolderExists) {
                sardine.createDirectory(url);
                logger.info("SardineStore() createdUrl=" + url);
            }
        } catch (IOException sardineException) {
            logger.warn("SardineStore() sardineException=" + sardineException, sardineException);
            throw new StoreException(sardineException);

        }
    }

    @Override
    public boolean has(String key) {
        logger.info("has(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            boolean exists = SardineFactory.begin(user, password).exists(url + key);
            logger.info("has(): exists=" + exists);
            return exists;
        } catch (IOException exception) {
            logger.error("has(): exception=" + exception, exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public long size(String key) {
        logger.info("size(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            List<DavResource> resources = SardineFactory.begin(user, password).list(url + key);
            logger.info("size(): resources=" + resources);
            Preconditions.checkNotNull(resources);
            Preconditions.checkArgument(resources.size() == 1);
            DavResource resource = resources.get(0);
            logger.info("size(): resource=" + resource);
            Preconditions.checkNotNull(resource);
            Long length = resource.getContentLength();
            logger.info("size(): length=" + length);
            Preconditions.checkNotNull(length);
            return length;
        } catch (IOException exception) {
            logger.error("size(): exception=" + exception, exception);
            throw new StoreException(exception);
        }
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
    public OutputStream save(String key) {
        logger.info("save(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            Path tempFile = Files.createTempFile("sardine-", ".tmp");
            logger.info("save(): tempFile=" + tempFile);
            return new BufferedOutputStream(Files.newOutputStream(tempFile)) {
                @Override
                public void close() throws IOException {
                    super.close();
                    logger.info("save().close(): tempFile=" + tempFile);
                    Sardine sardine = SardineFactory.begin(user, password);
                    sardine.enablePreemptiveAuthentication(URI.create(url).getHost());
                    sardine.put(url + key, Files.newInputStream(tempFile));
                    Files.delete(tempFile);
                    logger.info("save().close() saved. ");
                }
            };
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
