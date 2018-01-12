package us.cuatoi.s34jserver.core.helper;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        return new HashingInputStream(Hashing.md5(), Files.newInputStream(content)).hash().toString();
    }
}
