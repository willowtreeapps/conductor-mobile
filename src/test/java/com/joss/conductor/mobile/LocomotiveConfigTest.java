package com.joss.conductor.mobile;


import com.joss.conductor.mobile.config.LocomotiveConfig;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class LocomotiveConfigTest {

    @Test
    public void can_load_properties_file()  {
        LocomotiveConfig config = new LocomotiveConfig();
        config.loadProperties(this, "/simple.properties");

        Assertions.assertThat(config.getTimeout()).isEqualTo(10);
        Assertions.assertThat(config.getRetries()).isEqualTo(10);
        Assertions.assertThat(config.shouldScreenshotOnFail()).isTrue();
        Assertions.assertThat(config.isNoReset()).isTrue();
        Assertions.assertThat(config.getPlatformName()).isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getPackageFile()).isEqualTo("/apps/android/app.apk");
    }

    @Test
    public void second_load_replaces_only_specified_properties() {
        LocomotiveConfig config = new LocomotiveConfig();
        config.loadProperties(this, "/simple.properties");
        config.loadProperties(this, "/override.properties");

        Assertions.assertThat(config.getTimeout()).isEqualTo(10);
        Assertions.assertThat(config.getRetries()).isEqualTo(8);
        Assertions.assertThat(config.shouldScreenshotOnFail()).isFalse();
        Assertions.assertThat(config.isNoReset()).isFalse();
        Assertions.assertThat(config.getPlatformName()).isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getPackageFile()).isEqualTo("/apps/ios/ios.app");
    }

    @Test
    public void can_load_from_environment() {
        System.setProperty("conductorPlatformName", "IOS");
        System.setProperty("conductorRetries", "10");
        System.setProperty("conductorTimeout", "14");

        LocomotiveConfig config = new LocomotiveConfig();
        config.loadEnvironment();

        Assertions.assertThat(config.getPlatformName()).isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getRetries()).isEqualTo(10);
        Assertions.assertThat(config.getTimeout()).isEqualTo(14);
    }

    @Test
    public void load_from_environment_replaces_only_specified_properties() {
        System.setProperty("conductorPlatformName", "ANDROID");
        System.setProperty("conductorRetries", "8");
        System.setProperty("conductorTimeout", "14");

        LocomotiveConfig config = new LocomotiveConfig();
        config.loadProperties(this, "/simple.properties");
        config.loadEnvironment();

        Assertions.assertThat(config.getTimeout()).isEqualTo(14);
        Assertions.assertThat(config.getRetries()).isEqualTo(8);
        Assertions.assertThat(config.shouldScreenshotOnFail()).isTrue();
        Assertions.assertThat(config.isNoReset()).isTrue();
        Assertions.assertThat(config.getPlatformName()).isEqualByComparingTo(Platform.ANDROID);
        Assertions.assertThat(config.getPackageFile()).isEqualTo("/apps/android/app.apk");
    }
}
