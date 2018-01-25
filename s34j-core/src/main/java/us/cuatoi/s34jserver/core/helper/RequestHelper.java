package us.cuatoi.s34jserver.core.helper;

import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class RequestHelper {
    public static void printDebugInfo(Logger logger, HttpServletRequest request) {
        logger.trace("request.getMethod=" + request.getMethod());
        logger.trace("request.getPathInfo=" + request.getPathInfo());
        logger.trace("request.getRequestURI=" + request.getRequestURI());
        logger.trace("request.getRequestURL=" + request.getRequestURL());
        logger.trace("request.getServletPath=" + request.getServletPath());
        Enumeration<String> headerNames = request.getHeaderNames();
        logger.trace("request.getHeaders()=");
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            logger.trace("\t" + header + "=" + request.getHeader(header));
        }
    }
}
