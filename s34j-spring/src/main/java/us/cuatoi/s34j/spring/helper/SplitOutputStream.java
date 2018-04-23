package us.cuatoi.s34j.spring.helper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SplitOutputStream extends OutputStream {
    @SuppressWarnings("WeakerAccess")
    public static final int DEFAULT_SPLIT_SIZE = 6 * 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(SplitOutputStream.class);
    private final long splitSize;
    private final String tempFileName;
    private long totalBytes = 0;
    private long currentBytes = 0;
    private List<Path> parts = new ArrayList<Path>();
    private List<OutputStream> outputStreams = new ArrayList<>();
    private boolean closed = false;


    public SplitOutputStream() {
        this(DEFAULT_SPLIT_SIZE);
    }

    public SplitOutputStream(int splitSize) {
        this(splitSize, UUID.randomUUID().toString());
    }

    public SplitOutputStream(int splitSize, String tempFileName) {
        this.splitSize = splitSize;
        this.tempFileName = tempFileName;
    }

    @Override
    public void write(int b) throws IOException {
        OutputStream currentOutput = null;
        if (parts.size() == 0 || currentBytes >= splitSize) {
            currentOutput = newPart();
            currentBytes = 0;
        } else {
            currentOutput = outputStreams.get(outputStreams.size() - 1);
        }
        currentOutput.write(b);
        totalBytes++;
        currentBytes++;
    }

    private OutputStream newPart() throws IOException {
        Path part = Files.createTempFile(tempFileName + "-" + parts.size() + "-", ".tmp");
        logger.info("newPart() part=" + part);
        parts.add(part);
        OutputStream outputStream = Files.newOutputStream(part);
        outputStreams.add(outputStream);
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        super.close();
        logger.info("close() totalBytes=" + totalBytes);
        logger.info("close() currentBytes=" + currentBytes);
        int closedCount = 0;
        for (OutputStream outputStream : outputStreams) {
            closedCount++;
            outputStream.close();
        }
        logger.info("close() closedCount=" + closedCount);
        closed = true;
        logger.info("close() closed=" + closed);
    }

    public List<InputStream> getInputStreams() throws IOException {
        if (!closed) {
            throw new IllegalStateException("Please close the stream first.");
        }
        ArrayList<InputStream> streams = new ArrayList<>();
        for (Path part : parts) {
            streams.add(Files.newInputStream(part, StandardOpenOption.DELETE_ON_CLOSE));
        }
        return streams;
    }
}
