package com.joss.conductor.mobile.util;

/**
 * Created on 8/10/16.
 */
public class JvmUtil {
    public static String getJvmProperty(String prop) {
        return System.getProperty(prop, System.getenv(prop));
    }

    public static int getJvmIntProperty(String prop, int defaultValue) {
        String propValue = System.getProperty(prop, System.getenv(prop));
        if(propValue != null) {
            try {
                int value = Integer.parseInt(propValue);
                return value;
            } catch(NumberFormatException e) {
                Log.log.warning("Could not parse int from property '" + prop + "': " + e.toString());
            }
        }

        return defaultValue;
    }

    public static boolean getJvmBooleanProperty(String prop, boolean defaultValue) {
        String propValue = System.getProperty(prop, System.getenv(prop));
        if(propValue != null) {
            try {
                boolean value = Boolean.parseBoolean(propValue);
                return value;
            } catch(NumberFormatException e) {
                Log.log.warning("Could not parse boolean from property '" + prop + "': " + e.toString());
            }
        }

        return defaultValue;
    }
}
