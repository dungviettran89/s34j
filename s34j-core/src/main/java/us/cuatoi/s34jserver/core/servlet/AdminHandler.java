package us.cuatoi.s34jserver.core.servlet;

import com.google.common.collect.Iterators;
import com.google.common.net.MediaType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.StorageContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.apache.commons.lang3.StringUtils.*;

public class AdminHandler {

    private final StorageContext context;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Path staticResourcePath = Paths.get("s34j-core/src/main/resources/static");
    private final boolean development = Files.exists(staticResourcePath) && Files.isDirectory(staticResourcePath);

    public AdminHandler(StorageContext context) {
        this.context = context;
        if (development) {
            logger.debug("Development mode enabled. Loading file from " + staticResourcePath);
        }
    }

    public boolean service(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uri = request.getRequestURI();
        if (equalsIgnoreCase(uri, "/_admin/") ||
                endsWithAny(uri, ".html", ".css", ".js")) {
            return serviceStaticContent(uri, response);
        }
        return false;
    }

    private boolean serviceStaticContent(String uri, HttpServletResponse response) throws Exception {
        if (equalsIgnoreCase(uri, "/_admin/")) {
            logger.trace("Return index.html instead of /");
            uri = "/_admin/index.html";
        }

        //locate content
        try (InputStream is = getContent(uri)) {
            //return content
            if (is == null) return false;
            response.setStatus(HttpServletResponse.SC_OK);
            if (endsWith(uri, ".html")) {
                response.setContentType(MediaType.HTML_UTF_8.toString());
            } else if (endsWith(uri, ".js")) {
                response.setContentType(MediaType.JAVASCRIPT_UTF_8.toString());
            } else if (endsWith(uri, ".css")) {
                response.setContentType(MediaType.CSS_UTF_8.toString());
            }
            IOUtils.copy(is, response.getOutputStream());
            return true;
        }
    }

    private InputStream getContent(String uri) throws Exception {
        Path servingPath = Paths.get(ClassLoader.getSystemResource("/static" + uri).toURI());
//        if (development) {
//            servingPath = staticResourcePath
//                    .resolve(replace(uri, "/_admin/", "_admin/"));
//        }
        logger.debug("Serving " + servingPath);
        if (endsWithAny(uri, "all.js", "all.css")) {
            String extension = endsWith(uri, ".js") ? ".js" : ".css";
            Iterator<InputStream> streams = Files.walk(servingPath.getParent())
                    .sorted()
                    .filter((p) -> endsWith(p.toString(), extension))
                    .map((p) -> {
                        try {
                            return Files.newInputStream(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .iterator();
            return new SequenceInputStream(Iterators.asEnumeration(streams));
        } else {
            return Files.newInputStream(servingPath);
        }
    }
}
