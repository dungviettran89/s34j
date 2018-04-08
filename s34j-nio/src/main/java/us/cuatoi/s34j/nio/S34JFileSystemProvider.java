package us.cuatoi.s34j.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A distributed file system, which can span severals folder
 */
public class S34JFileSystemProvider {

    public static final String URI_SCHEME = "s34jfs";
    private static final Logger logger = LoggerFactory.getLogger(S34JFileSystemProvider.class);

    private S34JFileSystemProvider() {

    }
}
