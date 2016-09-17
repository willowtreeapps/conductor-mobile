package com.joss.conductor.mobile.util;

/**
 * Created on 8/10/16.
 */
public class JvmUtil {
    public static String getJvmProperty(String prop) {
        return System.getProperty(prop, System.getenv(prop));
    }
}
