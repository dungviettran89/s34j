package us.cuatoi.s34jserver.core.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SimpleStorageFilter implements Filter {

    private SimpleStorageContext context;
    private ServletHandler handler;


    @Override
    public void init(FilterConfig config) throws ServletException {
        String path = config.getInitParameter("path");
        if (isBlank(path) || !Files.exists(Paths.get(path))) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        String serverId = config.getInitParameter("serverId");
        if (isBlank(serverId)) {
            serverId = "s34j";
        }

        String region = config.getInitParameter("region");
        if (isBlank(region)) {
            region = "us-central-1";
        }

        String accessKey = config.getInitParameter("accessKey");
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
        handler = new ServletHandler(context);

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
