package us.cuatoi.s34jserver.core.helper;

import org.slf4j.Logger;

import static org.apache.commons.lang3.StringUtils.split;

public class LogHelper {
    public static void infoMultiline(Logger logger, String message) {
        for (String line : split(message, '\n')) {
            logger.info(line);
        }
    }

    public static void traceMultiline(Logger logger, String message) {
        for (String line : split(message, '\n')) {
            logger.trace(line);
        }
    }

    public static void debugMultiline(Logger logger, String message) {
        for (String line : split(message, '\n')) {
            logger.debug(line);
        }
    }
}
