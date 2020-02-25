package com.joss.conductor.mobile;

import com.saucelabs.common.SauceOnDemandAuthentication;

import org.openqa.selenium.InvalidArgumentException;
import org.pmw.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConductorConfig {
    private static final String DEFAULT_CONFIG_FILE = "/config.yaml";
    private static final String CUSTOM_CAPABILITIES = "customCapabilities";

    // Conductor properties
    private String[] currentSchemes;
    private int appiumRequestTimeout = 5;
    private int implicitWaitTime = 5;
    private boolean screenshotOnFail = true;
    private boolean screenshotOnSkip = false;

    // Appium Properties
    private Platform platformName = Platform.NONE;
    private String deviceName;
    private boolean autoGrantPermissions = true;
    private boolean noReset = true;
    private boolean fullReset = false;
    private String appiumVersion;
    private String platformVersion;
    private String appFile;
    private String language;
    private String locale;
    private String orientation;
    private String hub;
    private Boolean islocalhub = false;
    private String udid;
    private String automationName;
    private String appPackageName;
    private Map<String, Object> customCapabilities = new HashMap<>();
    private Boolean simpleIsVisibleCheck;

    // iOS specific
    private String xcodeSigningId;
    private String xcodeOrgId;
    private Boolean waitForQuiescence;

    // Android specific
    private String avd;
    private String avdArgs;
    private String appActivity;
    private String appWaitActivity;
    private String intentCategory;

    // SauceLabs Authentication
    private SauceOnDemandAuthentication authentication;
    private String sauceUserName;
    private String sauceAccessKey;

    // Timeouts
    private String newCommandTimeout;
    private String idleTimeout;
    private int startSessionRetries = 1; // by default try only once

    // dependencies
    private Map<String, String> environment;

    public ConductorConfig() {
        this(DEFAULT_CONFIG_FILE, System.getenv());
    }

    /**
     * Constructor method that takes in a relative path to a local resource config file
     *
     * @param localResourcesConfigPath Relative path to local config resource
     */
    public ConductorConfig(String localResourcesConfigPath) {
        this(localResourcesConfigPath, System.getenv());
    }

    /**
     * Constructor method that takes in a relative path to a local resource config file and custom environment variables
     *
     * @param localResourcesConfigPath Relative path to local config resource
     * @param environment              Map to use custom environment variables instead of provided by system
     */
    public ConductorConfig(String localResourcesConfigPath, Map<String, String> environment) {
        this.environment = environment;

        InputStream is = this.getClass().getResourceAsStream(localResourcesConfigPath);
        if (is != null) {
            readConfig(is);
        }
    }

    public ConductorConfig(InputStream yamlStream) {
        readConfig(yamlStream);
    }

    private void readConfig(InputStream is) {
        if (is == null) {
            throw new NullPointerException("InputStream parameter to readConfig cannot be null");
        }

        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(is);

        String environmentPlatformName = System.getProperty("conductorPlatformName");
        if (environmentPlatformName != null) {
            platformName = Platform.valueOf(environmentPlatformName);
        } else if (config.containsKey("platformName")) {
            platformName = Platform.valueOf((String) config.get("platformName"));
        }

        Map<String, Object> defaults = (Map<String, Object>) config.get("defaults");
        if (defaults != null) {
            readProperties(defaults);
            if (defaults.get("hub") != null) {
                setIsLocalHub(true);
            }
            Map<String, Object> platformDefaults = null;
            switch (platformName) {
                case IOS:
                    platformDefaults = (Map<String, Object>) defaults.get("ios");
                    break;
                case ANDROID:
                    platformDefaults = (Map<String, Object>) defaults.get("android");
                    break;
            }
            if (platformDefaults != null) {
                readProperties(platformDefaults);
            }
        }

        String environmentSchemes = System.getProperty("conductorCurrentSchemes");
        if (environmentSchemes != null) {
            currentSchemes = environmentSchemes.split(",");
        } else {
            List<String> schemesList = (List<String>) config.get("currentSchemes");
            if (schemesList != null) {
                currentSchemes = new String[schemesList.size()];
                schemesList.toArray(currentSchemes);
            }
        }

        if (currentSchemes != null) {

            for (String scheme : currentSchemes) {
                Map<String, Object> schemeData = (Map<String, Object>) config.get(scheme);
                if (schemeData != null) {
                    readProperties(schemeData);
                }
            }
        }
    }

    private void readProperties(Map<String, Object> properties) {
        for (String key : properties.keySet()) {
            if (key.equals("ios") || key.equals("android")) {
                // These keys are the start of platform specific options
                // and should be ignored
                continue;
            }

            if (key.equals(CUSTOM_CAPABILITIES)) {
                putCustomCapabilities(properties.get(key));
            } else if (key.equals("timeout") || key.equals("retries")) { //to automatically read deprecated
                String newKey = key.equals("timeout") ? "appiumRequestTimeout" : "implicitWaitTime";
                System.out.println("\"" + key + "\" has been deprecated. Please remove it from your config.yaml and replace it with \"" + newKey + "\"");
                setProperty(newKey, properties.get(key).toString());
            } else {
                setProperty(key, properties.get(key).toString());
            }
        }
    }

    private void setProperty(String propertyName, String propertyValue) {
        Method[] methods = this.getClass().getMethods();

        Pattern envPattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher envMatcher = envPattern.matcher(propertyValue);
        while (envMatcher.find()) {
            String env = envMatcher.group(1);
            String val = System.getProperty(env);

            // If the system property is not set '-Dsystem_property=<value>` then look for an environment variable
            if (val == null) {
                val = environment.get(env);
            }

            if (val != null) {
                propertyValue = propertyValue.replaceAll("\\$\\{" + env + "\\}", val);
            } else {
                Logger.warn("Could not find environment variable \"{}\" specified in config", env);
            }
        }

        String capPropertyKey = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        String methodName = "set" + capPropertyKey;
        Method foundMethod = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                foundMethod = method;
                break;
            }
        }

        if (foundMethod != null) {
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
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logger.warn(e, "Could not invoke method: $s", methodName);
            }
        }
    }

    public boolean getNoReset() {
        return noReset;
    }

    public void setNoReset(boolean noReset) {
        this.noReset = noReset;
    }

    public String getAppiumVersion() {
        return appiumVersion;
    }

    public void setAppiumVersion(String appiumVersion) {
        this.appiumVersion = appiumVersion;
    }

    /***
     * @deprecated since 0.19.1, use {@link #getAppiumRequestTimeout()} instead
     */
    @Deprecated
    public int getTimeout() {
        return appiumRequestTimeout;
    }

    public int getAppiumRequestTimeout() {
        return appiumRequestTimeout;
    }

    /***
     * @deprecated since 0.19.1, use {@link #setAppiumRequestTimeout(int)} instead
     */
    @Deprecated
    public void setTimeout(int timeout) {
        this.appiumRequestTimeout = timeout;
    }

    public void setAppiumRequestTimeout(int appiumRequestTimeout) {
        this.appiumRequestTimeout = appiumRequestTimeout;
    }

    /***
     * @deprecated since 0.19.1, use {@link #getImplicitWaitTime()} instead
     */
    @Deprecated
    public int getRetries() {
        return implicitWaitTime;
    }

    public int getImplicitWaitTime() {
        return implicitWaitTime;
    }

    /***
     * @deprecated since 0.19.1, use {@link #setImplicitWaitTime(int)}  instead
     */
    @Deprecated
    public void setRetries(int retries) {
        this.implicitWaitTime = retries;
    }

    public void setImplicitWaitTime(int implicitWaitTime) {
        this.implicitWaitTime = implicitWaitTime;
    }

    public boolean isFullReset() {
        return fullReset;
    }

    public void setFullReset(boolean fullReset) {
        this.fullReset = fullReset;
    }

    public String getAppFile() {
        return appFile;
    }

    public void setAppFile(String appFile) {
        this.appFile = appFile;
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

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public URL getHub() {
        URL url = null;
        if (hub != null) {
            try {
                url = new URL(hub);
            } catch (MalformedURLException e) {
                Logger.error(e, "Failure parsing Hub Url");
            }
        }

        return url;
    }

    public void setHub(String hub) {
        this.hub = hub;
    }

    public void setIsLocalHub(boolean isLocalHubValue) {
        this.islocalhub = isLocalHubValue;
    }


    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getAutomationName() {
        return automationName;
    }

    public void setAutomationName(String automationName) {
        this.automationName = automationName;
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

    public String getIntentCategory() {
        return intentCategory;
    }

    public void setIntentCategory(String intentCategory) {
        this.intentCategory = intentCategory;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public boolean isScreenshotOnFail() {
        return screenshotOnFail;
    }

    public void setScreenshotOnFail(boolean screenshotOnFail) {
        this.screenshotOnFail = screenshotOnFail;
    }

    public boolean isScreenshotOnSkip() {
        return screenshotOnSkip;
    }

    public void setScreenshotOnSkip(boolean screenshotOnSkip) {
        this.screenshotOnSkip = screenshotOnSkip;
    }

    public String getAvd() {
        return avd;
    }

    public void setAvd(String avd) {
        this.avd = avd;
    }

    public String getAvdArgs() {
        return avdArgs;
    }

    public void setAvdArgs(String avdArgs) {
        this.avdArgs = avdArgs;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isAutoGrantPermissions() {
        return autoGrantPermissions;
    }

    public void setAutoGrantPermissions(boolean autoGrantPermissions) {
        this.autoGrantPermissions = autoGrantPermissions;
    }

    public Boolean isWaitForQuiescence() {
        return waitForQuiescence;
    }

    public Boolean isSimpleIsVisibleCheck() {
        return simpleIsVisibleCheck;
    }

    public void setWaitForQuiescence(boolean waitForQuiescence) {
        this.waitForQuiescence = waitForQuiescence;
    }


    public String getSauceUserName() {
        return this.sauceUserName;
    }

    public String getSauceAccessKey() {
        return this.sauceAccessKey;
    }

    public String getNewCommandTimeout() {
        return this.newCommandTimeout;
    }

    public String getIdleTimeout() {
        return this.idleTimeout;
    }

    public void setSauceUserName(String sauceUserName) {
        this.sauceUserName = sauceUserName;
    }

    public void setSauceAccessKey(String sauceAccessKey) {
        this.sauceAccessKey = sauceAccessKey;
    }

    public void setNewCommandTimeout(String newCommandTimeout) {
        this.newCommandTimeout = newCommandTimeout;
    }

    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public void setSimpleIsVisibleCheck(boolean value) {
        this.simpleIsVisibleCheck = value;
    }

    public Platform getPlatformName() {
        return platformName;
    }

    public String[] getCurrentSchemes() {
        return currentSchemes;
    }

    public String getFullAppPath() {

        if (appFile == null) {
            return null;
        }

        File dir = new File(appFile);
        Path path = Paths.get(appFile);


        // Make sure apps can be hosted remotely, e.g. on a github repo for example, check for this first
        try {
            URL url = new URL(appFile);
            return appFile;
        } catch (MalformedURLException e) {
            // Ignore, this is expected to happen, parse as a regular path
        }

        // If sending directly to Saucelabs, we want to check for this case first because the app is not hosted locally
        if (appFile.contains("sauce-storage:") || path.isAbsolute()) {
            return appFile;
        }

        //if appFile end with these, assume we should try to use them
        if ((appFile.endsWith(".ipa") || appFile.endsWith(".apk") || appFile.endsWith(".app"))) {
            return Paths.get(System.getProperty("user.dir"), path.normalize().toString()).normalize().toString();
        }

        if (dir.isDirectory()) {
            try {
                String expectedFileExtension = platformName == Platform.ANDROID ? ".apk" : ".app";
                File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(expectedFileExtension));
                if ((files != null) && (files.length == 1)) {
                    File fileFoundMatchingExtension = files[0];
                    return Paths.get(System.getProperty("user.dir"), fileFoundMatchingExtension.toString()).normalize().toString();
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {
                String expectedFileExtension = platformName == Platform.ANDROID ? ".apk" : ".app";
                Logger.error(e, "Unable to find a valid application in specified appFile directory: [{}] \n\nIf there is more than one app per unique file extension in [{}], you must specify an absolute path." +
                        " Also, double check that there is indeed an application with a valid extension in the specified appFile directory. If in a folder, represent the root directory like so: [./apps/android.apk] " +
                        "\n\nFound the following files in the specified appFile: {} \n\nERROR", dir, dir, Arrays.toString(dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(expectedFileExtension))));
            }
        }

        // if all else fails, just return user.dir + appFile
        return Paths.get(System.getProperty("user.dir"), path.normalize().toString()).normalize().toString();
    }

    public void setCustomCapabilities(Map<String, Object> customCapabilities) {
        this.customCapabilities = customCapabilities;
    }

    public Map<String, Object> getCustomCapabilities() {
        return customCapabilities;
    }

    private void putCustomCapabilities(Object keyCustomCapabilities) {
        Map<String, Object> custom = new HashMap<>();
        if (keyCustomCapabilities instanceof Map) {
            for (String key : ((Map<String, Object>) keyCustomCapabilities).keySet()) {
                Object value = ((Map) keyCustomCapabilities).get(key);
                if (!(value instanceof Map)) {
                    custom.put(key, value);
                } else {
                    throw new ClassCastException(String.format("%s cannot be of type Map", value));
                }
            }
        } else {
            throw new ClassCastException(String.format("%s is expected to be a String list of key/value pairs", CUSTOM_CAPABILITIES));
        }
        putCustomCapabilities(custom);
    }

    private void putCustomCapabilities(Map<String, Object> customCapabilities) {
        this.customCapabilities.putAll(customCapabilities);
    }

    /**
     * Constructs a {@link SauceOnDemandAuthentication} instance using the supplied user name/access key.  To use the authentication
     * supplied by environment variables or from an external file, use the no-arg {@link SauceOnDemandAuthentication} constructor.
     */
    public SauceOnDemandAuthentication getSauceAuthentication(String sauceUserName, String sauceAccessKey) {

        if (sauceUserName == null) {
            throw new InvalidArgumentException("sauceUserName cannot be null");
        }
        if (sauceAccessKey == null) {
            throw new InvalidArgumentException("sauceAccessKey cannot be null");
        }

        return new SauceOnDemandAuthentication(sauceUserName, sauceAccessKey);
    }

    public boolean isLocal() {
        return getHub() == null;
    }

    public boolean isHubLocal() {
        return islocalhub;
    }

    public int getStartSessionRetries() {
        return startSessionRetries;
    }

    public void setStartSessionRetries(int startSessionRetries) {
        this.startSessionRetries = startSessionRetries;
    }
}
