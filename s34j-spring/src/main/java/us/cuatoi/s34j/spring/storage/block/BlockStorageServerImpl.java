package us.cuatoi.s34j.spring.storage.block;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.spring.helper.InputStreamWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.apache.commons.lang3.StringUtils.endsWith;

public class BlockStorageServerImpl implements BlockStorage {
    private static final Logger logger = LoggerFactory.getLogger(BlockStorageServerImpl.class);
    private final String baseUrl;

    public BlockStorageServerImpl(String baseUrl) {
        this.baseUrl = baseUrl + (endsWith(baseUrl, "/") ? "" : "/");
        logger.info("BlockStorageServerImpl() baseUrl=" + this.baseUrl);
    }

    @Override
    public long save(String key, InputStream input) throws IOException {
        logger.info("save() key=" + key);
        HttpURLConnection connection = getConnection(key);
        try {
            connection.setRequestMethod("PUT");
            connection.connect();
            long length = ByteStreams.copy(input, connection.getOutputStream());
            ByteStreams.copy(connection.getInputStream(), ByteStreams.nullOutputStream());
            logger.info("save() length=" + length);
            return length;
        } finally {
            connection.disconnect();
        }
    }

    @Override
    public InputStream load(String key) throws IOException {
        logger.info("load() key=" + key);
        HttpURLConnection connection = getConnection(key);
        connection.setRequestMethod("PUT");
        connection.connect();
        return new InputStreamWrapper(connection.getInputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                connection.disconnect();
            }
        };
    }

    @Override
    public void delete(String key) throws IOException {
        logger.info("delete() key=" + key);
        HttpURLConnection connection = getConnection(key);
        connection.setRequestMethod("DELETE");
        connection.connect();
        try (InputStream inputStream = connection.getInputStream()) {
            ByteStreams.copy(inputStream, ByteStreams.nullOutputStream());
        } finally {
            connection.disconnect();
        }
    }

    protected HttpURLConnection getConnection(String key) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + key)
                .openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }
}
