package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.JvmUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Properties;

/**
 * Created on 8/10/16.
 *
 * Order of overrides:
 * <ol>
 *   <li>JVM Arguments</li>
 *   <li>Test</li>
 *   <li>Default properties</li>
 * </ol>
 */
public class LocomotiveConfig implements Config {

    public static int DEFAULT_MAX_RETRIES = 5;
    public static int DEFAULT_MAX_TIMEOUT = 5;

    private Config testConfig;
    private Properties properties;

    public LocomotiveConfig(Config testConfig, Properties properties) {
        this.testConfig = testConfig;
        this.properties = properties;
    }

    public String appPackageName() {
        return getStringValue(Constants.DEFAULT_PROPERTY_APP_PACKAGE_NAME,
                testConfig == null ? null : testConfig.appPackageName(),
                Constants.JVM_CONDUCTOR_APP_PACKAGE_NAME);
    }

    public Platform platformName() {
        return getPlatformValue(Constants.DEFAULT_PROPERTY_PLATFORM_NAME,
                testConfig == null ? null : testConfig.platformName(),
                Constants.JVM_CONDUCTOR_PLATFORM_NAME);
    }

    public String platformVersion() {
        return getStringValue(Constants.DEFAULT_PROPERTY_PLATFORM_VERSION,
                testConfig == null ? null : testConfig.platformVersion(),
                Constants.JVM_CONDUCTOR_PLATFORM_VERSION);
    }

    public String deviceName() {
        return getStringValue(Constants.DEFAULT_PROPERTY_DEVICE_NAME,
                testConfig == null ? null : testConfig.deviceName(),
                Constants.JVM_CONDUCTOR_DEVICE_NAME);
    }

    public String apk() {
        return getStringValue(Constants.DEFAULT_PROPERTY_APK,
                testConfig == null ? null : testConfig.apk(),
                Constants.JVM_CONDUCTOR_APK);
    }

    public String ipa() {
        return getStringValue(Constants.DEFAULT_PROPERTY_IPA,
                testConfig == null ? null : testConfig.ipa(),
                Constants.JVM_CONDUCTOR_IPA);
    }

    public String udid() {
        return getStringValue(Constants.DEFAULT_PROPERTY_UDID,
                testConfig == null ? null : testConfig.udid(),
                Constants.JVM_CONDUCTOR_UDID);
    }

    public String appiumVersion() {
        return getStringValue(Constants.DEFAULT_PROPERTY_APPIUM_VERSION,
                testConfig == null ? null : testConfig.appiumVersion(),
                Constants.JVM_CONDUCTOR_APPIUM_VERSION);
    }

    public String language() {
        return getStringValue(Constants.DEFAULT_PROPERTY_LANGUAGE,
                testConfig == null ? null : testConfig.language(),
                Constants.JVM_CONDUCTOR_LANGUAGE);
    }

    public String locale() {
        return getStringValue(Constants.DEFAULT_PROPERTY_LOCALE,
                testConfig == null ? null : testConfig.locale(),
                Constants.JVM_CONDUCTOR_LOCALE);
    }

    public String orientation() {
        return getStringValue(Constants.DEFAULT_PROPERTY_ORIENTATION,
                testConfig == null ? null : testConfig.orientation(),
                Constants.JVM_CONDUCTOR_ORIENTATION);
    }

    public boolean autoWebView() {
        return getBooleanValue(Constants.DEFAULT_PROPERTY_AUTO_WEBVIEW,
                Constants.JVM_CONDUCTOR_AUTO_WEBVIEW);
    }

    public boolean noReset() {
        return getBooleanValue(Constants.DEFAULT_PROPERTY_NO_RESET,
                Constants.JVM_CONDUCTOR_NO_RESET);
    }

    public boolean fullReset() {
        return getBooleanValue(Constants.DEFAULT_PROPERTY_FULL_RESET,
                Constants.JVM_CONDUCTOR_FULL_RESET);
    }

    public boolean autoAcceptAlerts() {
        return getBooleanValue(Constants.DEFAULT_PROPERTY_AUTO_ACCEPT_ALERTS,
                Constants.JVM_CONDUCTOR_AUTO_ACCEPT_ALERTS);
    }

    public String hub() {
        return getStringValue(Constants.DEFAULT_PROPERTY_HUB,
                testConfig == null ? null : testConfig.hub(),
                Constants.JVM_CONDUCTOR_HUB);
    }

    public int timeout() {
        return getIntValue(Constants.DEFAULT_PROPERTY_TIMEOUT,
                testConfig == null ? null : testConfig.timeout(),
                Constants.JVM_CONDUCTOR_TIMEOUT,
                DEFAULT_MAX_TIMEOUT);
    }

    public int retries() {
        return getIntValue(Constants.DEFAULT_PROPERTY_RETRIES,
                testConfig == null ? null : testConfig.retries(),
                Constants.JVM_CONDUCTOR_RETRIES,
                DEFAULT_MAX_RETRIES);
    }

    public boolean screenshotsOnFail() {
        return getBooleanValue(Constants.DEFAULT_PROPERTY_SCREENSHOTS_ON_FAIL,
                Constants.JVM_CONDUCTOR_SCREENSHOTS_ON_FAIL);
    }

    public boolean autoGrantPermissions() {
        return getBooleanValue(Constants.DEFAULT_PROPERTY_AUTO_GRANT_PERMISSIONS,
                Constants.JVM_CONDUCTOR_AUTO_GRANT_PERMISSIONS);
    }

    public String automationName() {
        return getStringValue(Constants.DEFAULT_PROPERTY_AUTOMATION_NAME,
                testConfig == null ? null : testConfig.automationName(),
                Constants.JVM_CONDUCTOR_AUTOMATION_NAME);
    }

    public Class<? extends Annotation> annotationType() {
        return null;
    }

    private String getStringValue(String defaultPropertyKey, String testConfigValue, String jvmParamKey) {
        String value = "";
        String defaultValue = getProperty(defaultPropertyKey);
        String jvmValue = JvmUtil.getJvmProperty(jvmParamKey);

        if(defaultValue != null && !StringUtils.isEmpty(defaultValue)) {
            value = defaultValue;
        }
        if(testConfigValue != null && !StringUtils.isEmpty(testConfigValue)) {
            value = testConfigValue;
        }
        if(jvmValue != null && !StringUtils.isEmpty(jvmValue)) {
            value = jvmValue;
        }
        return value;
    }

    private Platform getPlatformValue(String defaultPropertyKey, Platform testConfigValue, String jvmParamKey) {
        Platform value = Platform.NONE;
        String defaultValue = getProperty(defaultPropertyKey);
        String jvmValue = JvmUtil.getJvmProperty(jvmParamKey);

        if(defaultValue != null && !StringUtils.isEmpty(defaultValue)) {
            value = Platform.valueOf(defaultValue);
        }
        if(testConfigValue != null && testConfigValue != Platform.NONE) {
            value = testConfigValue;
        }
        if(jvmValue != null && !StringUtils.isEmpty(jvmValue)) {
            value = Platform.valueOf(jvmValue);
        }
        return value;
    }

    private boolean getBooleanValue(String defaultPropertyKey, String jvmParamKey) {
        boolean value = false;
        String defaultValue = getProperty(defaultPropertyKey, Boolean.FALSE.toString());
        String jvmValue = JvmUtil.getJvmProperty(jvmParamKey);

        if(defaultValue != null && !StringUtils.isEmpty(defaultValue)) {
            value = Boolean.valueOf(defaultValue);
        }
        if(jvmValue != null && !StringUtils.isEmpty(jvmValue)) {
            value = Boolean.valueOf(jvmValue);
        }
        return value;
    }

    private int getIntValue(String defaultPropertyKey, Integer testConfigValue, String jvmParamKey, int defaultValue) {
        int value = defaultValue;
        String defaultPropValue = getProperty(defaultPropertyKey, String.valueOf(defaultValue));
        String jvmValue = JvmUtil.getJvmProperty(jvmParamKey);

        if(defaultPropValue != null && !StringUtils.isEmpty(defaultPropValue)) {
            value = Integer.valueOf(defaultPropValue);
        }
        if(testConfigValue != null) {
            value = testConfigValue;
        }
        if(jvmValue != null && !StringUtils.isEmpty(jvmValue)) {
            value = Integer.valueOf(jvmValue);
        }
        return value;
    }

    private String getProperty(String key) {
        return getProperty(key, "");
    }

    private String getProperty(String key, String defaultValue) {
        return properties == null ? "" : properties.getProperty(key, defaultValue);
    }

    public String getAppFullPath() {
        String app;
        switch (platformName()) {
            case ANDROID:
                app = apk();
                break;
            case IOS:
                app = ipa();
                break;
            default:
                throw new IllegalArgumentException("Unknown platform: " + platformName());
        }
        return System.getProperty("user.dir") + app;
    }
}
