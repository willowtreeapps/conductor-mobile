package com.joss.conductor.mobile.config;

import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;

enum ConfigProperty {
    // Conductor parameters
    PLATFORM_NAME("platformName", null),
    HUB("hub", null),
    TIMEOUT("timeout", null),
    RETRIES("retries", null),
    SCREENSHOT_ON_FAIL("screenshotOnFail", null),
    LOG_LEVEL("logLevel", null),

    // Appium Parameters
    AUTO_GRANT_PERMISSIOSN("autoGrantPermissions", "autoGrantPermissions"),
    AUTOMATION_NAME("automationName", MobileCapabilityType.AUTOMATION_NAME),
    PLATFORM_VERSION("platformVersion", MobileCapabilityType.PLATFORM_VERSION),
    DEVICE_NAME("deviceName", MobileCapabilityType.DEVICE_NAME),
    UDID("udid", MobileCapabilityType.UDID),
    PACKAGE_FILE("packageFile", MobileCapabilityType.APP),
    LANGUAGE("language", MobileCapabilityType.LANGUAGE),
    LOCALE("locale", MobileCapabilityType.LOCALE),
    ORIENTATION("orientation", MobileCapabilityType.ORIENTATION),
    AUTO_WEBVIEW("autoWebview", MobileCapabilityType.AUTO_WEBVIEW),
    NO_RESET("noReset", MobileCapabilityType.NO_RESET),
    FULL_RESET("fullReset", MobileCapabilityType.FULL_RESET),

    // Android specific
    AVD("avd", AndroidMobileCapabilityType.AVD),
    APP_ACTIVITY("appActivity", AndroidMobileCapabilityType.APP_ACTIVITY),
    APP_WAIT_ACTIVITY("appWaitActivity", AndroidMobileCapabilityType.APP_WAIT_ACTIVITY),
    INTENT_CATEGORY("intentCategory", AndroidMobileCapabilityType.INTENT_CATEGORY),

    // iOS specific
    AUTO_ACCEPT_ALERTS("autoAcceptAlerts", IOSMobileCapabilityType.AUTO_ACCEPT_ALERTS),
    XCODE_SIGNING_ID("xcodeSigningId", "xcodeSigningId"),
    XCODE_ORG_ID("xcodeOrgId", "xcodeOrgId");

    private String conductorKey;
    private String appiumKey;

    public String conductorKey() { return conductorKey; }
    public String getAppiumKey() { return appiumKey; }

    ConfigProperty(String conductorValue, String appiumValue) {
        this.conductorKey = conductorValue;
        this.appiumKey = appiumValue;
    }
}
