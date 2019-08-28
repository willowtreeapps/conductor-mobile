package com.joss.conductor.mobile;

import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ConductorCapabilities {

    private static ConductorConfig config = new ConductorConfig("/test_yaml/all_platforms.yaml");

    public static DesiredCapabilities build() {
        return build(null);
    }

    public static DesiredCapabilities build(DesiredCapabilities capabilities) {
        if (capabilities == null) {
            capabilities = new DesiredCapabilities();
        }

        switch (config.getPlatformName()) {
            case ANDROID:
            case IOS:
                capabilities.setCapability(MobileCapabilityType.UDID, config.getUdid());
                capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, config.getDeviceName());
                capabilities.setCapability(MobileCapabilityType.APP, config.getFullAppPath());
                capabilities.setCapability(MobileCapabilityType.ORIENTATION, config.getOrientation());
                capabilities.setCapability("autoGrantPermissions", config.isAutoGrantPermissions());
                capabilities.setCapability(MobileCapabilityType.FULL_RESET, config.isFullReset());
                capabilities.setCapability(MobileCapabilityType.NO_RESET, config.getNoReset());
                capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, config.getPlatformVersion());
                capabilities.setCapability("xcodeSigningId", config.getXcodeSigningId());
                capabilities.setCapability("xcodeOrgId", config.getXcodeOrgId());
                capabilities.setCapability(AndroidMobileCapabilityType.AVD, config.getAvd());
                capabilities.setCapability(AndroidMobileCapabilityType.AVD_ARGS, config.getAvdArgs());
                capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, config.getAppActivity());
                capabilities.setCapability(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY, config.getAppWaitActivity());
                capabilities.setCapability(AndroidMobileCapabilityType.INTENT_CATEGORY, config.getIntentCategory());
                capabilities.setCapability("sauceUserName", config.getSauceUserName());
                capabilities.setCapability("sauceAccessKey", config.getSauceAccessKey());
                capabilities.setCapability("waitForQuiescence", config.isWaitForQuiescence());
                capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, config.getNewCommandTimeout());
                capabilities.setCapability("idleTimeout", config.getIdleTimeout());
                capabilities.setCapability("simpleIsVisibleCheck", config.isSimpleIsVisibleCheck());
                capabilities.setCapability(MobileCapabilityType.APPIUM_VERSION, config.getAppiumVersion());

                if (StringUtils.isNotEmpty(config.getAutomationName())) {
                    capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, config.getAutomationName());
                }


                // Set custom capabilities if there are any
                for (String key : config.getCustomCapabilities().keySet()) {
                    capabilities.setCapability(key, config.getCustomCapabilities().get(key));
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown platform: " + config.getPlatformName());
        }

        // If deviceName is empty replace it with something
        Object deviceName = capabilities.getCapability(MobileCapabilityType.DEVICE_NAME);
        if (deviceName == null || deviceName.toString().isEmpty()) {
            capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Empty Device Name");
        }

        return capabilities;
    }
}
