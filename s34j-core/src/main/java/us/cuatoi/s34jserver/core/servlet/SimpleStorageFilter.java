package us.cuatoi.s34jserver.core.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SimpleStorageFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private SimpleStorageContext context;
    private ServletHandler handler;


    @Override
    public void init(FilterConfig config) throws ServletException {
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

    private String getConfiguration(FilterConfig config, String name, String defaultValue) {
        String region = config.getInitParameter(name);
        if (isBlank(region)) {
            region = defaultValue;
        }
        return region;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!handler.service((HttpServletRequest) request, (HttpServletResponse) response)) {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
