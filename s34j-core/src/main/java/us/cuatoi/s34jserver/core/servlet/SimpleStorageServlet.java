package us.cuatoi.s34jserver.core.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SimpleStorageServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private SimpleStorageContext context;
    private ServletHandler handler;

    @Override
    public void init() throws ServletException {
        ServletConfig config = getServletConfig();
        String path = config.getInitParameter("path");
        logger.info("path=" + Paths.get(path).toAbsolutePath().toString());
        if (isBlank(path) || !Files.exists(Paths.get(path))) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        String serverId = getConfiguration(config, "serverId", "s34j");
        logger.info("serverId=" + serverId);
        String region = getConfiguration(config, "region", "us-central-1");
        logger.info("region=" + region);
        String adminEnabled = getConfiguration(config, "adminEnabled", "true");
        logger.info("adminEnabled=" + adminEnabled);
        String accessKey = config.getInitParameter("accessKey");
        logger.info("accessKey=" + accessKey);
        String secretKey = config.getInitParameter("secretKey");
        if (isAnyBlank(accessKey, secretKey)) {
            throw new IllegalArgumentException("Please configure access key and secret key");
        }

        context = new SimpleStorageContext();
        context.setBaseDir(Paths.get(path));
        context.setAccessKey(accessKey);
        context.setSecretKey(secretKey);
        context.setServerId(serverId);
        context.setRegion(region);
        context.setAdminEnabled("true".equalsIgnoreCase(adminEnabled));
        handler = new ServletHandler(context);
    }

    private String getConfiguration(ServletConfig config, String name, String defaultValue) {
        String value = config.getInitParameter(name);
        return isBlank(value) ? defaultValue : value;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (contains(req.getRequestURI(), "favicon.ico")) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else if (!handler.service(req, resp)) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
}
