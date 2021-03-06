package us.cuatoi.s34jserver.core.handler.bucket;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.StringUtils.length;
import static org.apache.commons.lang3.StringUtils.substring;

public class ObjectVisitor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Path baseDir;
    private String delimiter;
    private long maxKeys = 1000;
    private String prefix;
    private String continuationToken;
    private String startAfter;
    private String suffix;


    private boolean isTruncated = false;
    private List<Path> objects = new ArrayList<>();
    private List<String> prefixes = new ArrayList<>();
    private String nextContinuationToken;
    private String nextMarker;

    /* Constructors */
    public ObjectVisitor(Path baseDir) {
        this.baseDir = baseDir;
    }

    /* Getters */
    public boolean isTruncated() {
        return isTruncated;
    }

    public List<Path> getObjects() {
        return objects;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public String getNextContinuationToken() {
        return nextContinuationToken;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public long getMaxKeys() {
        return maxKeys;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public String getStartAfter() {
        return startAfter;
    }

    /* Setters */
    public ObjectVisitor setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public ObjectVisitor setMaxKeys(long maxKeys) {
        this.maxKeys = maxKeys;
        return this;
    }

    public ObjectVisitor setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public ObjectVisitor setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }

    public ObjectVisitor setStartAfter(String startAfter) {
        this.startAfter = startAfter;
        return this;
    }

    /* Functions */
    public ObjectVisitor visit() throws IOException {
        visit(baseDir);
        isTruncated = isNotBlank(nextContinuationToken);
        return this;
    }

    private void visit(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            visitDir(path);
        } else {
            visitFile(path);
        }
    }

    private void visitFile(Path file) {
        String fileName = getName(file);

        if (handlePrefix(fileName)) return;

        if (handleNextTokens(fileName)) {
            return;
        }

        if (isNotBlank(suffix)) {
            if (!endsWith(fileName, suffix)) {
                logger.trace("Skip due to suffix: " + fileName);
                return;
            }
        }

        if (handleDelimiter(fileName)) {
            logger.trace("Skip due to delimiter: " + fileName);
            return;
        }

        if (objects.size() < maxKeys) {
            logger.trace("Found: " + fileName);
            objects.add(file);
            nextMarker = fileName;
        } else {
            if (isBlank(nextContinuationToken)) {
                nextContinuationToken = fileName;
            }
            logger.trace("Skip due to truncated: " + fileName);
        }
    }

    private void visitDir(Path dir) throws IOException {
        String dirName = getName(dir);

        //check prefix
        if (handlePrefix(dirName)) return;

        if (handleDelimiter(dirName)) {
            logger.trace("Skip due to delimiter: " + dirName);
            return;
        }
        if (isTruncated) {
            logger.trace("Skip due to truncated: " + dirName);
            return;
        }
        List<Path> sortedChilds = Files.list(dir)
                .sorted((p1, p2) -> StringUtils.compare(p1.toString(), p2.toString()))
                .collect(Collectors.toList());
        for (Path child : sortedChilds) {
            visit(child);
        }
    }

    private boolean handleNextTokens(String dirName) {
        if (isNotBlank(continuationToken) && isNotBlank(dirName)) {
            if (compare(dirName, continuationToken) < 0) {
                logger.trace("Skip due to continuationToken: " + dirName);
                return true;
            }
        } else if (isNotBlank(startAfter)) {
            if (compare(dirName, startAfter) <= 0) {
                logger.trace("Skip due to startAfter: " + dirName);
                return true;
            }
        }
        return false;
    }

    private boolean handlePrefix(String dirName) {
        if (isNotBlank(prefix)) {
            boolean notContainsPrefix = !contains(dirName, prefix);
            boolean notPrefixContains = !contains(prefix, dirName);
            if (notContainsPrefix && notPrefixContains) {
                logger.trace("Skip due to prefix: " + dirName);
                return true;
            }
        }
        return false;
    }

    private String getName(Path dir) {
        String relativePath = baseDir.relativize(dir).toString();
        String separator = baseDir.getFileSystem().getSeparator();
        if (!equalsIgnoreCase(separator, "/")) {
            return replace(relativePath, separator, "/");
        } else {
            return relativePath;
        }

    }

    private boolean handleDelimiter(String fileName) {
        if (isNotBlank(delimiter)) {
            String prefixToCheck = trimToEmpty(prefix);
            String fileNameToCheck = fileName;
            if (isNotBlank(suffix) && endsWith(fileNameToCheck, suffix)) {
                fileNameToCheck = substring(fileName, 0, length(fileNameToCheck) - length(suffix));
            }
            int indexOfDelimiter = indexOf(fileNameToCheck, delimiter, prefixToCheck.length());
            if (indexOfDelimiter >= 0) {
                String pathPrefix = substring(fileNameToCheck, 0, indexOfDelimiter + length(delimiter));
                if (!prefixes.contains(pathPrefix)) {
                    prefixes.add(pathPrefix);
                }
                return true;
            }
        }
        return false;
    }

    public String getNextMarker() {
        return nextMarker;
    }

    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    public String getSuffix() {
        return suffix;
    }

    public ObjectVisitor setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }
}
