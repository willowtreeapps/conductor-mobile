package com.joss.conductor.mobile;

import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ConductorConfigTest {

    @AfterMethod
    public void teardown() {
        System.clearProperty("conductorPlatformName");
        System.clearProperty("conductorCurrentSchemes");
    }

    @Test
    public void no_config_reads_default_yaml()  {
        ConductorConfig config = new ConductorConfig();

        String[] expectedSchemes = {"scheme1", "scheme2"};

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.ANDROID);
        Assertions.assertThat(config.getCurrentSchemes())
                .isEqualTo(expectedSchemes);
    }

    @Test
    public void config_supplied_reads_supplied_config() {
        ConductorConfig config = new ConductorConfig("/test_yaml/simple.yaml");

        String[] expectedSchemes = {"test_scheme1", "test_scheme2"};

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getCurrentSchemes())
                .isEqualTo(expectedSchemes);
    }

    @Test
    public void config_reads_defaults() {
        ConductorConfig config = new ConductorConfig("/test_yaml/simple_defaults.yaml");

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getNoReset())
                .isFalse();
        Assertions.assertThat(config.getAppiumVersion())
                .isEqualTo("1.7.1");
        Assertions.assertThat(config.getTimeout())
                .isEqualTo(8);
        Assertions.assertThat(config.getRetries())
                .isEqualTo(10);
    }

    @Test
    public void config_reads_defaults_for_android() {
        ConductorConfig config = new ConductorConfig("/test_yaml/android_defaults.yaml");

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.ANDROID);
        // Tests that platform overrides defaults
        Assertions.assertThat(config.getRetries())
                .isEqualTo(4);
        Assertions.assertThat(config.getAppFile())
                .isEqualTo("./apps/android.apk");
        Assertions.assertThat(config.getAppActivity())
                .isEqualTo("com.android.activity");
    }

    @Test
    public void config_reads_defaults_for_ios() {
        ConductorConfig config = new ConductorConfig("/test_yaml/ios_defaults.yaml");

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getRetries())
                .isEqualTo(2);
        Assertions.assertThat(config.getAppFile())
                .isEqualTo("./apps/ios.app");
        Assertions.assertThat(config.getXcodeSigningId())
                .isEqualTo("iPhone Developer");
        Assertions.assertThat(config.getXcodeOrgId())
                .isEqualTo(("TEAMID"));
    }

    @Test
    public void config_overrides_with_current_schemes() {
        ConductorConfig config = new ConductorConfig("/test_yaml/schemes.yaml");

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getCurrentSchemes())
                .isEqualTo(new String[] { "longer_timeouts", "ios_device" });
        Assertions.assertThat(config.getRetries())
                .isEqualTo(3);
        Assertions.assertThat(config.getTimeout())
                .isEqualTo(20);
        Assertions.assertThat(config.getAppFile())
                .isEqualTo("./apps/ios.ipa");
    }

    @Test
    public void config_overrides_schemes_in_order() {
        ConductorConfig config = new ConductorConfig("/test_yaml/override_schemes.yaml");

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getCurrentSchemes())
                .isEqualTo(new String[] { "longer_timeouts", "ios_saucelabs" });
        Assertions.assertThat(config.getRetries())
                .isEqualTo(1);
        Assertions.assertThat(config.getTimeout())
                .isEqualTo(20);
        Assertions.assertThat(config.getAppFile())
                .isEqualTo("sauce-storage:mock.zip");
    }

    @Test
    public void environment_platform_name_overrides_config() {
        System.setProperty("conductorPlatformName", "IOS");
        ConductorConfig config = new ConductorConfig("/test_yaml/all_platforms.yaml");

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getAppFile())
                .isEqualTo("./apps/ios.app");
    }

    @Test
    public void environment_schemes_overrides_config() {
        System.setProperty("conductorCurrentSchemes", "shorter_timeouts,android_device");
        ConductorConfig config = new ConductorConfig("/test_yaml/all_platforms.yaml");

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.ANDROID);
        Assertions.assertThat(config.getRetries())
                .isEqualTo(8);
        Assertions.assertThat(config.getTimeout())
                .isEqualTo(5);
        Assertions.assertThat(config.getUdid())
                .isEqualTo("auto");
    }

    @Test
    public void full_app_path_expands_relative() {
        ConductorConfig config = new ConductorConfig("/test_yaml/all_platforms.yaml");

        Assertions.assertThat(config.getFullAppPath())
                .isEqualTo(System.getProperty("user.dir") + "/apps/android.apk");
    }

    @Test
    public void sauce_storage_scheme_does_not_resolve_path() {
        ConductorConfig config = new ConductorConfig("/test_yaml/all_platforms.yaml");
        config.setAppFile("sauce-storage:app.zip");

        Assertions.assertThat(config.getFullAppPath())
                .isEqualTo("sauce-storage:app.zip");
    }

    @Test
    public void absolute_paths_are_kept() {
        ConductorConfig config = new ConductorConfig("/test_yaml/all_platforms.yaml");
        config.setAppFile("/Users/username/code/path/app.ipa");

        Assertions.assertThat(config.getFullAppPath())
                .isEqualTo("/Users/username/code/path/app.ipa");
    }

    @Test
    public void urls_do_not_resolve_path() {
        ConductorConfig config = new ConductorConfig("/test_yaml/all_platforms.yaml");
        config.setAppFile("https://willowtreeapps.com/app.zip");

        Assertions.assertThat(config.getFullAppPath())
                .isEqualTo("https://willowtreeapps.com/app.zip");
    }

    @Test
    public void custom_capabilities_defaults() {
        ConductorConfig config = new ConductorConfig("/test_yaml/android_defaults_custom_caps.yaml");

        Map<String, Object> customCapabilities = new HashMap<>();
        customCapabilities.put("foo", "bar");
        customCapabilities.put("fizz", "buzz");
        customCapabilities.put("truty", true);

        Assertions.assertThat(config.getCustomCapabilities())
                .containsAllEntriesOf(customCapabilities);
    }

    @Test
    public void custom_capabilities_defaults_overridden_by_scheme() {
        ConductorConfig config = new ConductorConfig("/test_yaml/android_defaults_custom_caps_schemes.yaml");

        Map<String, Object> customCapabilities = new HashMap<>();
        customCapabilities.put("foo", "foo");
        customCapabilities.put("fizz", "fizz");

        Assertions.assertThat(config.getCustomCapabilities())
                .containsAllEntriesOf(customCapabilities);
    }
}
