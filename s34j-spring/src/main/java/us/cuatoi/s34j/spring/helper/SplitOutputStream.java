package us.cuatoi.s34j.spring.helper;


import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SplitOutputStream extends OutputStream {
    public static final Logger logger = LoggerFactory.getLogger(SplitOutputStream.class);
    private long splitSize = 5 * 1024 * 1024;
    private long totalBytes = 0;
    private long currentBytes = 0;
    private List<FileObject> parts = new ArrayList<>();
    private List<OutputStream> outputStreams = new ArrayList<>();
    private boolean closed = false;

    public SplitOutputStream(long splitSize) {
        this.splitSize = splitSize;
    }

    @Override
    public void write(int b) throws IOException {
        OutputStream currentOutput = null;
        if (parts.size() == 0 || currentBytes >= splitSize) {
            currentOutput = newPart();
        } else {
            currentOutput = outputStreams.get(outputStreams.size() - 1);
        }
        currentOutput.write(b);
        totalBytes++;
        currentBytes++;
    }

    private OutputStream newPart() throws IOException {
        FileObject part = VFS.getManager().resolveFile("tmp://" + UUID.randomUUID().toString() + ".tmp");
        logger.info("newPart() part=" + part);
        parts.add(part);
        OutputStream outputStream = part.getContent().getOutputStream();
        outputStreams.add(outputStream);
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        super.close();
        closed = true;
        for (OutputStream outputStream : outputStreams) {
            outputStream.close();
        }
    }

}
