package tez2.util;

import org.apache.log4j.Logger;
import org.joda.time.LocalTime;
import tez2.environment.context.DayPart;

/**
 * Created by suat on 21-Jun-17.
 */
public class LogUtil {
    public static void log_info(Logger logger, String deviceIdentifier, String message) {
        logger.info(deviceIdentifier + " " + message);
    }

    public static void log_debug(Logger logger, String deviceIdentifier, String message) {
        logger.debug(deviceIdentifier + " " + message);
    }

    public static void log_generic(Logger logger, String message) {
        logger.info(message);
    }
}
