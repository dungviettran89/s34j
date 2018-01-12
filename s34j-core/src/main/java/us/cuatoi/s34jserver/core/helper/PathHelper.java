package us.cuatoi.s34jserver.core.helper;

import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PathHelper {

    public static final Logger logger = LoggerFactory.getLogger(PathHelper.class);

    public static void deleteDir(Path dir) throws IOException {
        final List<Path> pathsToDelete = Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        for (Path path : pathsToDelete) {
            Files.deleteIfExists(path);
        }
    }

    @SuppressWarnings("deprecation")
    public static String md5HashFile(Path content) throws IOException {
        byte[] bytes = md5HashFileToByte(content);
        return BaseEncoding.base16().encode(bytes).toLowerCase();
    }

    public static byte[] md5HashFileToByte(Path content) throws IOException {
        return hash(content, "MD5");
    }

    private static byte[] hash(Path content, String algorithm) throws IOException {
        try (InputStream is = Files.newInputStream(content)) {
            MessageDigest md5Digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[16 * 1024];
            int len = is.read(buffer);
            while (len > 0) {
                md5Digest.update(buffer, 0, len);
                len = is.read(buffer);
            }
            return md5Digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha256HashFile(Path content) throws IOException {
        byte[] bytes = hash(content, "SHA-256");
        return BaseEncoding.base16().encode(bytes).toLowerCase();
    }
}
