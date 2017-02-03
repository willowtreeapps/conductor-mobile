package com.joss.conductor.mobile;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created on 8/8/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Config {
    Platform platformName() default Platform.NONE;
    String appPackageName() default "";
    String platformVersion() default "";
    String deviceName() default "";
    String apk() default "";
    String ipa() default "";
    String udid() default "";
    String appiumVersion() default "";
    String language() default "";
    String locale() default "";
    String orientation() default "";
    String hub() default "";
    int timeout() default 5;
    int retries() default 5;
    String automationName() default "";
}
