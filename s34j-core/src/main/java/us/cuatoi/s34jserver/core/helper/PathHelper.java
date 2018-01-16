package us.cuatoi.s34jserver.core.helper;

import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static us.cuatoi.s34jserver.core.S3Constants.EXPIRATION_DATE_FORMAT;

public class PathHelper {

    public static final Logger logger = LoggerFactory.getLogger(PathHelper.class);
    public static final Logger LOGGER = LoggerFactory.getLogger(PathHelper.class);

    public static void deleteDir(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
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

    public static String getLastModifiedString(Path path) throws IOException {
        return EXPIRATION_DATE_FORMAT.print(Files.getLastModifiedTime(path).toMillis());
    }

    public static String getLastModifiedStringUnchecked(Path path) {
        try {
            return getLastModifiedString(path);
        } catch (IOException e) {
            LOGGER.info("Can get last modified:" + path, e);
            return null;
        }
    }

    public static String getCreationTimeString(Path path) throws IOException {
        BasicFileAttributes attribute = Files.readAttributes(path, BasicFileAttributes.class);
        return EXPIRATION_DATE_FORMAT.print(attribute.creationTime().toMillis());
    }

    public static long sizeUnchecked(Path part) {
        try {
            return Files.size(part);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
