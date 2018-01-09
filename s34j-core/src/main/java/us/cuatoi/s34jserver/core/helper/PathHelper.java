package us.cuatoi.s34jserver.core.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class PathHelper {

    public static final Logger logger = LoggerFactory.getLogger(PathHelper.class);

    public static void deleteDir(Path dir) throws IOException {
        Files.walk(dir).sorted(Comparator.reverseOrder())
                .forEach((f) -> {
                    try {
                        Files.deleteIfExists(f);
                        logger.info("Deleted " + f);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
