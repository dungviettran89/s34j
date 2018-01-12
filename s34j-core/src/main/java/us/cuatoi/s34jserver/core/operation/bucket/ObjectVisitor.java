package us.cuatoi.s34jserver.core.operation.bucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;

class ObjectVisitor extends SimpleFileVisitor<Path> {
    private final Path baseDir;
    private String delimiter;
    private long maxKeys = 1000;
    private String prefix;
    private String continuationToken;
    private String startAfter;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private boolean isTruncated = false;
    private List<Path> objects = new ArrayList<>();
    private List<String> prefixes = new ArrayList<>();
    private String nextContinuationToken;

    public ObjectVisitor(Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        dir = normalize(dir);
        String dirName = getName(dir);
        logger.debug("Checking dir : " + dirName);

        //check prefix
        if (isNotBlank(prefix)) {
            boolean prefixContains = contains(prefix, dirName);
            boolean startWithPrefix = indexOf(dirName, prefix) == 0;
            if (!prefixContains && !startWithPrefix) {
                return FileVisitResult.SKIP_SUBTREE;
            }
        }

        if (handleDelimiter(dirName)) return FileVisitResult.SKIP_SUBTREE;


        return isTruncated ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        file = normalize(file);
        String fileName = getName(file);
        logger.debug("Checking file: " + fileName);

        if (isNotBlank(prefix)) {
            if (indexOf(fileName, prefix) != 0) {
                return FileVisitResult.CONTINUE;
            }
            if (compare(fileName.substring(0, prefix.length()), prefix) > 0) {
                return FileVisitResult.SKIP_SIBLINGS;
            }
        }

        if (isNotBlank(continuationToken)) {
            if (compare(fileName, continuationToken) < 0) {
                return FileVisitResult.CONTINUE;
            }
        } else if (isNotBlank(startAfter)) {
            if (compare(fileName, startAfter) <= 0) {
                return FileVisitResult.CONTINUE;
            }
        }

        if (isTruncated) {
            if (isBlank(nextContinuationToken)) {
                nextContinuationToken = fileName;
            }
            return FileVisitResult.SKIP_SIBLINGS;
        }

        if (handleDelimiter(fileName)) return FileVisitResult.SKIP_SIBLINGS;

        if (objects.size() < maxKeys) {
            objects.add(file);
        }
        isTruncated = objects.size() >= maxKeys;
        return FileVisitResult.CONTINUE;
    }

    private boolean handleDelimiter(String fileName) {
        if (isNotBlank(delimiter)) {
            String prefixToCheck = trimToEmpty(prefix);
            int indexOfDelimiter = indexOf(fileName, delimiter, prefixToCheck.length());
            if (indexOfDelimiter >= 0) {
                String pathPrefix = substring(fileName, 0, indexOfDelimiter + length(delimiter));
                if (!prefixes.contains(pathPrefix)) {
                    prefixes.add(pathPrefix);
                }
                return true;
            }
        }
        return false;
    }

    private String getName(Path dir) {
        return replace(dir.toString(), "\\", "/");
    }

    private Path normalize(Path file) {
        return baseDir.relativize(file).normalize();
    }

    public ObjectVisitor setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public ObjectVisitor setMaxKeys(long maxKeys) {
        this.maxKeys = maxKeys;
        return this;
    }

    public long getMaxKeys() {
        return maxKeys;
    }

    public ObjectVisitor setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public ObjectVisitor setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public ObjectVisitor setStartAfter(String startAfter) {
        this.startAfter = startAfter;
        return this;
    }

    public String getStartAfter() {
        return startAfter;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isTruncated() {
        return isTruncated;
    }

    public List<Path> getObjects() {
        return objects;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public ObjectVisitor visit() throws IOException {
        Files.walkFileTree(baseDir, this);
        return this;
    }

    public String getNextContinuationToken() {
        return nextContinuationToken;
    }

    public void setNextContinuationToken(String nextContinuationToken) {
        this.nextContinuationToken = nextContinuationToken;
    }
}
