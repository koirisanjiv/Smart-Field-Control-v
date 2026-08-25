package com.qaverse.smart.FieldAccessControl.Logging;

import java.util.function.Supplier;

import com.qaverse.smart.logger.SmartLog;

/**
 * Internal logging gateway for Smart Field Control.
 *
 * <p>
 * Smart Field Control logging is disabled by default.
 * </p>
 *
 * <p>
 * System property:
 * {@code smart.field.control.logging}
 * </p>
 */
public final class FieldControlLogger {

    private static final String PROPERTY =
            "smart.field.control.logging";

    private static volatile boolean enabled =
            Boolean.parseBoolean(
                    System.getProperty(PROPERTY, "false")
            );

    private FieldControlLogger() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable Smart Field Control logging.
     */
    public static void setEnabled(boolean value) {
        enabled = value;
    }

    /**
     * Re-read the system property.
     */
    public static void refresh() {
        enabled = Boolean.parseBoolean(
                System.getProperty(PROPERTY, "false")
        );
    }

    public static void debug(String message) {

        if (enabled) {
            SmartLog.debug(message);
        }
    }

    public static void debug(Supplier<String> message) {

        if (enabled) {
            SmartLog.debug(message);
        }
    }

    public static void warn(String message) {

        if (enabled) {
            SmartLog.warn(message);
        }
    }

    public static void warn(Supplier<String> message) {

        if (enabled) {
            SmartLog.warn(message);
        }
    }

    public static void error(String message) {

        if (enabled) {
            SmartLog.error(message);
        }
    }

    public static void error(Supplier<String> message) {

        if (enabled) {
            SmartLog.error(message);
        }
    }

    public static void success(String message) {

        if (enabled) {
            SmartLog.success(message);
        }
    }

    public static void step(String message) {

        if (enabled) {
            SmartLog.step(message);
        }
    }
    
    public static void error(
            String message,
            Throwable throwable) {

        if (enabled) {
            SmartLog.error(message, throwable);
        }
    }

    public static void error(
            Supplier<String> message,
            Throwable throwable) {

        if (enabled) {
            SmartLog.error(message, throwable);
        }
    }
    
    public static void setLoggingEnabled(boolean enabled) {
        FieldControlLogger.setEnabled(enabled);
    }
    
    public static void refreshLoggingConfiguration() {
        FieldControlLogger.refresh();
    }
}