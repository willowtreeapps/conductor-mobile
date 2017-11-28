package com.joss.conductor.mobile;

import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.InputStream;

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
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/simple.yaml");
        ConductorConfig config = new ConductorConfig(is);

        String[] expectedSchemes = {"test_scheme1", "test_scheme2"};

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getCurrentSchemes())
                .isEqualTo(expectedSchemes);
    }

    @Test
    public void config_reads_defaults() {
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/simple_defaults.yaml");
        ConductorConfig config = new ConductorConfig(is);

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
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/android_defaults.yaml");
        ConductorConfig config = new ConductorConfig(is);

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
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/ios_defaults.yaml");
        ConductorConfig config = new ConductorConfig(is);

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
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/schemes.yaml");
        ConductorConfig config = new ConductorConfig(is);

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
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/override_schemes.yaml");
        ConductorConfig config = new ConductorConfig(is);

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
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/all_platforms.yaml");
        ConductorConfig config = new ConductorConfig(is);

        Assertions.assertThat(config.getPlatformName())
                .isEqualByComparingTo(Platform.IOS);
        Assertions.assertThat(config.getAppFile())
                .isEqualTo("./apps/ios.app");
    }

    @Test
    public void environment_schemes_overrides_config() {
        System.setProperty("conductorCurrentSchemes", "shorter_timeouts,android_device");
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/all_platforms.yaml");
        ConductorConfig config = new ConductorConfig(is);

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
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/all_platforms.yaml");
        ConductorConfig config = new ConductorConfig(is);

        Assertions.assertThat(config.getFullAppPath())
                .isEqualTo(System.getProperty("user.dir") + "/apps/android.apk");
    }

    @Test
    public void sauce_storage_scheme_does_not_resolve_path() {
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/all_platforms.yaml");
        ConductorConfig config = new ConductorConfig(is);
        config.setAppFile("sauce-storage:app.zip");

        Assertions.assertThat(config.getFullAppPath())
                .isEqualTo("sauce-storage:app.zip");
    }

    @Test
    public void absolute_paths_are_kept() {
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/all_platforms.yaml");
        ConductorConfig config = new ConductorConfig(is);
        config.setAppFile("/Users/username/code/path/app.ipa");

        Assertions.assertThat(config.getFullAppPath())
                .isEqualTo("/Users/username/code/path/app.ipa");
    }

    @Test
    public void urls_do_not_resolve_path() {
        InputStream is = this.getClass().getResourceAsStream("/test_yaml/all_platforms.yaml");
        ConductorConfig config = new ConductorConfig(is);
        config.setAppFile("https://willowtreeapps.com/app.zip");

        Assertions.assertThat(config.getFullAppPath())
                .isEqualTo("https://willowtreeapps.com/app.zip");
    }
}
