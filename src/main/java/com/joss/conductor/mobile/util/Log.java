package com.joss.conductor.mobile.util;

import com.joss.conductor.mobile.Locomotive;

import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created on 9/14/16.
 */
public class Log {

    public static Logger log;

    static {
        log = LogManager.getLogManager().getLogger(Locomotive.class.getSimpleName());
    }

    public static void fatal(String message) {
        log.severe(message);
    }

}
