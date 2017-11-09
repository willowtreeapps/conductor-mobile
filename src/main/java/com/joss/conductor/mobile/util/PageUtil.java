package com.joss.conductor.mobile.util;

import com.joss.conductor.mobile.ConductorConfig;
import org.openqa.selenium.By;

/**
 * Created on 9/2/16.
 */
public class PageUtil {

    private static final String ANDROID_APP_PACKAGE_NAME_ID = "%s:id/%s";
    private static final String IOS_XPATH_ACCESSIBILITY_IDENTIFIER = "//*[@name='%s']";

    public static By buildBy(ConductorConfig config, String identifier) {
        switch (config.getPlatformName()) {
            case ANDROID:
                return By.id(String.format(ANDROID_APP_PACKAGE_NAME_ID, config.getAppPackageName(), identifier));
            case IOS:
                return By.xpath(String.format(IOS_XPATH_ACCESSIBILITY_IDENTIFIER, identifier));
            default:
                System.err.println("Unknown platform: " + config.getPlatformName());
                System.exit(1);
        }
        return null;
    }

}
