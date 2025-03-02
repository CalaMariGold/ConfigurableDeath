package com.calamarigold.configurabledeath.util;

import com.calamarigold.configurabledeath.ConfigurableDeath;
import com.calamarigold.configurabledeath.config.ModConfig;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for handling mod logging with config-based control
 */
public class ModLogger {
    private static final Logger LOGGER = ConfigurableDeath.LOGGER;
    private static final String LOG_PREFIX = "[Configurable Death] ";
    
    /**
     * Log an info message that will always be shown regardless of detailed logging setting
     * @param message The message to log
     * @param params Optional parameters for formatting
     */
    public static void info(String message, Object... params) {
        LOGGER.info(LOG_PREFIX + message, params);
    }
    
    /**
     * Log a debug message that will only be shown if detailed logging is enabled
     * @param message The message to log
     * @param params Optional parameters for formatting
     */
    public static void debug(String message, Object... params) {
        if (ModConfig.enableDetailedLogging.get()) {
            LOGGER.info(LOG_PREFIX + message, params);
        }
    }
    
    /**
     * Log a warning message that will always be shown regardless of detailed logging setting
     * @param message The message to log
     * @param params Optional parameters for formatting
     */
    public static void warn(String message, Object... params) {
        LOGGER.warn(LOG_PREFIX + message, params);
    }
    
    /**
     * Log an error message that will always be shown regardless of detailed logging setting
     * @param message The message to log
     * @param params Optional parameters for formatting
     */
    public static void error(String message, Object... params) {
        LOGGER.error(LOG_PREFIX + message, params);
    }
} 