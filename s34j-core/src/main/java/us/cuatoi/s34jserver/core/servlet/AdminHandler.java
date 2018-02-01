package us.cuatoi.s34jserver.core.servlet;

import com.google.common.net.MediaType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import us.cuatoi.s34jserver.core.StorageContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.lang3.StringUtils.*;

public class AdminHandler {

    private final StorageContext context;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AdminHandler(StorageContext context) {
        this.context = context;
    }

    public boolean service(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try (InputStream inputStream = getInputStream(request.getRequestURI())) {
            if (inputStream == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return true;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            if (endsWithIgnoreCase(request.getRequestURI(), ".html")) {
                response.setContentType(MediaType.HTML_UTF_8.toString());
            } else if (endsWithIgnoreCase(request.getRequestURI(), ".js")) {
                response.setContentType(MediaType.JSON_UTF_8.toString());
            } else if (endsWithIgnoreCase(request.getRequestURI(), ".css")) {
                response.setContentType(MediaType.CSS_UTF_8.toString());
            }
            IOUtils.copy(inputStream, response.getOutputStream());
            return true;
        }
    }

    private InputStream getInputStream(String uri) throws IOException {
        uri = equalsIgnoreCase(uri, "/_admin/") ? "/_admin/index.html" : uri;
        try {
            ClassPathResource resource = new ClassPathResource("/static" + uri);
            return resource.exists() ? resource.getInputStream() : null;
        } catch (NoClassDefFoundError | Exception ex) {
            logger.debug("Cannot locate using spring:", ex);
            return getClass().getResourceAsStream("/static" + uri);
        }
    }
}
