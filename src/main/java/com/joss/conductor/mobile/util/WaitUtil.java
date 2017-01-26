package com.joss.conductor.mobile.util;

import org.assertj.core.api.Assertions;

/**
 * Created on 12/29/16.
 */
public class WaitUtil {

    public static void wait(int millis, String message) {
        try {
            System.out.println(message);
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage(), e);
        }
    }
}
