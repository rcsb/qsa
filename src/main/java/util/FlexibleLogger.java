package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonin Pavelka
 */
public class FlexibleLogger {

    private static FlexibleLogger fl = new FlexibleLogger();

    private FlexibleLogger() {

    }

    public static void error(String msg, Throwable t) {
        Logger logger = LoggerFactory.getLogger(fl.getClass());
        logger.error(msg, t);
    }

    public static void error(Throwable t) {
        Logger logger = LoggerFactory.getLogger(fl.getClass());
        logger.error("", t);
    }

    public static void error(String msg) {
        Logger logger = LoggerFactory.getLogger(fl.getClass());
        logger.error(msg);
    }
}
