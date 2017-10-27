package com.joss.conductor.mobile.config;

import com.joss.conductor.mobile.Platform;
import com.joss.conductor.mobile.util.Log;
import com.joss.conductor.mobile.util.PropertiesUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.joss.conductor.mobile.util.JvmUtil.getJvmProperty;
import static com.joss.conductor.mobile.util.JvmUtil.getJvmBooleanProperty;
import static com.joss.conductor.mobile.util.JvmUtil.getJvmIntProperty;

/**
 * Created on 8/10/16.
 *
 * Order of overrides:
 * <ol>
 *   <li>Default</li>
 *   <li>JVM Arguments</li>
 *   <li>LocomotiveConfig</li>
 * </ol>
 */
public class LocomotiveConfig  {

    public static int DEFAULT_MAX_RETRIES = 5;
    public static int DEFAULT_MAX_TIMEOUT = 5;

    // Locomotive Info
    private int timeout = DEFAULT_MAX_TIMEOUT;
    private int retries = DEFAULT_MAX_RETRIES;
    private boolean screenshotOnFail = false;
    
    // Appium info 
    private String logLevel = "debug";

    private String appiumVersion;
    private String hub;
    private boolean noReset = false;
    private boolean fullReset = false;
    private boolean autoAcceptAlerts = true;
    private boolean autoGrantPermissions = true;

    // Platform settings
    private Platform platformName = Platform.NONE;
    private String platformVersion;
    private String deviceName;
    private String udid;
    private String orientation;
    private String automationName;

    // App Settings
    private String appPackageName;
    
    /**
     * The name of the package file. This can be an iOS ipa or .app, or and Android apk
     */
    private String packageFile;
    private String language;
    private String locale;
    
    // iOS Specific
    private String xcodeSigningId;
    private String xcodeOrgId;

    // Android Specific
    private String avd;
    private String appActivity;
    private String appWaitActivity;

    public LocomotiveConfig() {

    }

    public void loadProperties(Object object, String propertiesFile)
    {
        Properties props = PropertiesUtil.getProperties(object, propertiesFile);
        for(ConfigProperty property: ConfigProperty.values()) {
            String propertyValue = props.getProperty(property.conductorKey());
            if(propertyValue != null) {
                setProperty(property.conductorKey(), propertyValue);
            }
        }
    }

    public void loadEnvironment()  {
        for(ConfigProperty property: ConfigProperty.values()) {
            String propertyName = property.conductorKey();
            String capPropertyKey = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            String propertyValue = System.getProperty("conductor" + capPropertyKey);
            if(propertyValue != null) {
                setProperty(property.conductorKey(), propertyValue);
            }
        }
    }

    public void loadPlatformFromEnvironment() {
        String propertyValue = System.getProperty("conductorPlatformName");
        if(propertyValue != null) {
            Platform value = Platform.valueOf(propertyValue);
            setPlatformName(value);
        }
    }

    private void setProperty(String propertyName, String propertyValue) {
        Method[] methods = this.getClass().getMethods();

        String capPropertyKey = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        String methodName = "set" + capPropertyKey;
        Method foundMethod = null;
        for(Method method : methods) {
            if(method.getName().equals(methodName)) {
                foundMethod = method;
                break;
            }
        }

        if(foundMethod != null) {
            try {
                if (foundMethod.getParameterTypes()[0] == String.class) {
                    foundMethod.invoke(this, propertyValue);
                } else if (foundMethod.getParameterTypes()[0] == boolean.class) {
                    boolean value = Boolean.parseBoolean(propertyValue);
                    foundMethod.invoke(this, value);
                } else if (foundMethod.getParameterTypes()[0] == int.class) {
                    int value = Integer.parseInt(propertyValue);
                    foundMethod.invoke(this, value);
                } else if (foundMethod.getParameterTypes()[0] == Platform.class) {
                    Platform value = Platform.valueOf(propertyValue);
                    foundMethod.invoke(this, value);
                }
            } catch(IllegalAccessException e) {
                Log.log.warning("Could not invoke method '" + methodName + "': " + e.toString());
            } catch (InvocationTargetException e) {
                Log.log.warning("Could not invoke method '" + methodName + "': " + e.toString());
            }
        }
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getAppiumVersion() {
        return appiumVersion;
    }

    public void setAppiumVersion(String appiumVersion) {
        this.appiumVersion = appiumVersion;
    }

    public String getHub() {
        return hub;
    }

    public void setHub(String hub) {
        this.hub = hub;
    }

    public String getAppFullPath() {
        return System.getProperty("user.dir") + packageFile;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean shouldScreenshotOnFail() {
        return screenshotOnFail;
    }

    public void setScreenshotOnFail(boolean screenshotOnFail) {
        this.screenshotOnFail = screenshotOnFail;
    }

    public boolean isNoReset() {
        return noReset;
    }

    public void setNoReset(boolean noReset) {
        this.noReset = noReset;
    }

    public boolean isFullReset() {
        return fullReset;
    }

    public void setFullReset(boolean fullReset) {
        this.fullReset = fullReset;
    }

    public boolean isAutoAcceptAlerts() {
        return autoAcceptAlerts;
    }

    public void setAutoAcceptAlerts(boolean autoAcceptAlerts) {
        this.autoAcceptAlerts = autoAcceptAlerts;
    }

    public boolean isAutoGrantPermissions() {
        return autoGrantPermissions;
    }

    public void setAutoGrantPermissions(boolean autoGrantPermissions) {
        this.autoGrantPermissions = autoGrantPermissions;
    }

    public Platform getPlatformName() {
        return platformName;
    }

    public void setPlatformName(Platform platformName) {
        this.platformName = platformName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getAutomationName() {
        return automationName;
    }

    public void setAutomationName(String automationName) {
        this.automationName = automationName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getPackageFile() {
        return packageFile;
    }

    public void setPackageFile(String packageFile) {
        this.packageFile = packageFile;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getXcodeSigningId() {
        return xcodeSigningId;
    }

    public void setXcodeSigningId(String xcodeSigningId) {
        this.xcodeSigningId = xcodeSigningId;
    }

    public String getXcodeOrgId() {
        return xcodeOrgId;
    }

    public void setXcodeOrgId(String xcodeOrgId) {
        this.xcodeOrgId = xcodeOrgId;
    }

    public String getAppActivity() {
        return appActivity;
    }

    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
    }

    public String getAppWaitActivity() {
        return appWaitActivity;
    }

    public void setAppWaitActivity(String appWaitActivity) {
        this.appWaitActivity = appWaitActivity;
    }

    public String getAvd() {
        return avd;
    }

    public void setAvd(String avd) {
        this.avd = avd;
    }
}
