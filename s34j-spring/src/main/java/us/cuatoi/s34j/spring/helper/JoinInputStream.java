package us.cuatoi.s34j.spring.helper;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JoinInputStream extends InputStream {
    public static final Logger logger = LoggerFactory.getLogger(JoinInputStream.class);
    private static final ExecutorService opener = Executors.newCachedThreadPool();
    private final List<Callable<InputStream>> streams;
    private int next = 1;
    private InputStream currentStream;
    private Future<InputStream> nextStream;

    public JoinInputStream(List<Callable<InputStream>> streams) throws IOException {
        this.streams = streams;
        logger.info("JoinInputStream() streams = " + streams);
        Preconditions.checkNotNull(streams);
        Preconditions.checkArgument(streams.size() > 0);

        try {
            currentStream = this.streams.get(0).call();
        } catch (Exception getStreamError) {
            handleError(getStreamError);
        }

        next();
    }

    private void handleError(Exception getStreamError) throws IOException {
        logger.error("JoinInputStream() getStreamError=" + getStreamError);
        if (getStreamError instanceof IOException) {
            throw (IOException) getStreamError;
        }
        throw new IOException(getStreamError);
    }

    private void next() {
        logger.info("next() next=" + next);
        if (this.streams.size() > next) {
            nextStream = opener.submit(this.streams.get(next));
            next++;
        } else {
            nextStream = null;
        }
        logger.info("next() nextStream=" + nextStream);
    }


    @Override
    public int read() throws IOException {
        int read = currentStream.read();
        if (read < 0 && nextStream != null) {
            try {
                currentStream = new BufferedInputStream(nextStream.get());
            } catch (Exception nextStreamError) {
                handleError(nextStreamError);
            }
            next();
        }
        return read;
    }
}
