package com.joss.conductor.mobile.util;

import org.assertj.core.api.Assertions;
import org.pmw.tinylog.Logger;

/**
 * Created on 12/29/16.
 */
public class WaitUtil {

    public static void wait(int millis, String message) {
        try {
            Logger.info(message);
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage(), e);
        }
    }
}
