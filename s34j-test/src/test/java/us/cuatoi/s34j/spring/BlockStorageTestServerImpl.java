package us.cuatoi.s34j.spring;

import com.google.common.io.ByteStreams;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.storage.block.BlockStorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

@Service
public class BlockStorageTestServerImpl implements BlockStorage {

    private final FileObject tempDir;

    public BlockStorageTestServerImpl() throws FileSystemException {
        tempDir = VFS.getManager().resolveFile(URI.create("tmp://test-blocks/"));
        tempDir.createFolder();
    }

    @Override
    public long save(String key, InputStream input) throws IOException {
        try (InputStream is = input) {
            try (OutputStream os = tempDir.resolveFile(key).getContent().getOutputStream()) {
                return ByteStreams.copy(is, os);
            }
        }
    }

    @Override
    public InputStream load(String key) throws IOException {
        return tempDir.resolveFile(key).getContent().getInputStream();
    }

    @Override
    public void delete(String key) throws IOException {
        tempDir.resolveFile(key).delete();
    }
}
