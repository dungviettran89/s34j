package us.cuatoi.s34jserver.core.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SimpleStorageServlet extends HttpServlet {

    private SimpleStorageContext context;
    private ServletHandler handler;

    @Override
    public void init() throws ServletException {
        ServletConfig config = getServletConfig();
        String path = config.getInitParameter("path");
        if (isBlank(path) || !Files.exists(Paths.get(path))) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        String serverId = config.getInitParameter("serverId");
        if (isBlank(serverId)) {
            serverId = "s34j";
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
        handler = new ServletHandler(context);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (handler.service(req, resp)) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
}
