package us.cuatoi.s34j.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import us.cuatoi.s34j.spring.storage.SpringStorageService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

public class SpringStorageServlet extends HttpServlet {

    public static final Logger logger = LoggerFactory.getLogger(SpringStorageServlet.class);
    @Autowired
    private SpringStorageService springStorageService;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            springStorageService.handle(request, response);
        } catch (URISyntaxException unexpectedError) {
            logger.error("service() unexpectedError=" + unexpectedError, unexpectedError);
            throw new IOException(unexpectedError);
        }
    }
}
