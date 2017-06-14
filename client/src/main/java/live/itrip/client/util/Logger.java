package live.itrip.client.util;

/**
 * Created by JianF on 2015/12/27.
 */
public class Logger {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Logger.class.getName());

    public static void debug(Object message) {
        logger.debug(message);
    }

    public static void debug(Object message, Throwable throwable) {
        logger.debug(message, throwable);
    }

    public static void info(Object message) {
        logger.info(message);
    }

    public static void info(Object message, Throwable throwable) {
        logger.info(message, throwable);
    }

    public static void warn(Object message) {
        logger.warn(message);
    }

    public static void warn(Object message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public static void error(Object message) {
        logger.error(message);
    }

    public static void error(Object message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
