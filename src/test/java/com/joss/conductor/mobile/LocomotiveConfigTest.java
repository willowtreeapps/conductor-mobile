package com.joss.conductor.mobile;

import org.assertj.core.api.ThrowableAssert;
import org.assertj.swing.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 9/14/16.
 */
public class LocomotiveConfigTest {

    private Config testConfig;
    private Properties defaultProperties;

    @Before
    public void setup() {
        defaultProperties = new Properties();
        defaultProperties.setProperty(Constants.DEFAULT_PROPERTY_APP_PACKAGE_NAME, "com.joss.conductor.mobile.default");
        defaultProperties.setProperty(Constants.DEFAULT_PROPERTY_PLATFORM_NAME, Platform.ANDROID.name());
        defaultProperties.setProperty(Constants.DEFAULT_PROPERTY_AUTO_WEBVIEW, Boolean.TRUE.toString());
        defaultProperties.setProperty(Constants.DEFAULT_PROPERTY_TIMEOUT, String.valueOf(15));
        defaultProperties.setProperty(Constants.DEFAULT_PROPERTY_AUTO_ACCEPT_ALERTS, Boolean.TRUE.toString());

        testConfig = mock(Config.class);
        when(testConfig.platformName()).thenReturn(Platform.IOS);
        when(testConfig.appPackageName()).thenReturn("com.joss.conductor.mobile.test.andoridConfig");
        when(testConfig.autoWebView()).thenReturn(false);
        when(testConfig.timeout()).thenReturn(10);
        when(testConfig.autoAcceptAlerts()).thenReturn(true);
    }

    @After
    public void teardown() {
        System.clearProperty(Constants.JVM_CONDUCTOR_APP_PACKAGE_NAME);
        System.clearProperty(Constants.JVM_CONDUCTOR_PLATFORM_NAME);
        System.clearProperty(Constants.JVM_CONDUCTOR_AUTO_WEBVIEW);
        System.clearProperty(Constants.JVM_CONDUCTOR_TIMEOUT);
        System.clearProperty(Constants.JVM_CONDUCTOR_TIMEOUT);
    }

    @Test
    public void test_default_string_properties() {
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.appPackageName())
                .isEqualTo("com.joss.conductor.mobile.default");
    }

    @Test
    public void test_config_override_default_string_properties() {
        LocomotiveConfig config = new LocomotiveConfig(testConfig, defaultProperties);
        Assertions.assertThat(config.appPackageName())
                .isEqualTo("com.joss.conductor.mobile.test.andoridConfig");
    }

    @Test
    public void test_jvm_overrides_test_config_string_properties() {
        System.setProperty(Constants.JVM_CONDUCTOR_APP_PACKAGE_NAME, "com.joss.conductor.mobile.jvm");
        LocomotiveConfig config = new LocomotiveConfig(testConfig, null);
        Assertions.assertThat(config.appPackageName())
                .isEqualTo("com.joss.conductor.mobile.jvm");
    }

    @Test
    public void test_jvm_overrides_default_string_properties() {
        System.setProperty(Constants.JVM_CONDUCTOR_APP_PACKAGE_NAME, "com.joss.conductor.mobile.jvm");
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.appPackageName())
                .isEqualTo("com.joss.conductor.mobile.jvm");
    }

    @Test
    public void test_default_platform_property() {
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.platformName())
                .isEqualTo(Platform.ANDROID);
    }

    @Test
    public void test_config_override_default_platform_property() {
        LocomotiveConfig config = new LocomotiveConfig(testConfig, defaultProperties);
        Assertions.assertThat(config.platformName())
                .isEqualTo(Platform.IOS);
    }

    @Test
    public void test_jvm_overrides_test_config_platform_property() {
        System.setProperty(Constants.JVM_CONDUCTOR_PLATFORM_NAME, Platform.ANDROID.name());
        LocomotiveConfig config = new LocomotiveConfig(testConfig, null);
        Assertions.assertThat(config.platformName())
                .isEqualTo(Platform.ANDROID);
    }

    @Test
    public void test_jvm_overrides_default_platform_property() {
        System.setProperty(Constants.JVM_CONDUCTOR_PLATFORM_NAME, Platform.IOS.name());
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.platformName())
                .isEqualTo(Platform.IOS);
    }

    @Test
    public void test_default_boolean_properties() {
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.autoWebView())
                .isTrue();
    }

    @Test
    public void test_config_override_default_boolean_properties() {
        LocomotiveConfig config = new LocomotiveConfig(testConfig, defaultProperties);
        Assertions.assertThat(config.autoWebView())
                .isFalse();
    }

    @Test
    public void test_jvm_overrides_test_config_boolean_properties() {
        System.setProperty(Constants.JVM_CONDUCTOR_AUTO_WEBVIEW, Boolean.TRUE.toString());
        LocomotiveConfig config = new LocomotiveConfig(testConfig, null);
        Assertions.assertThat(config.autoWebView())
                .isTrue();
    }

    @Test
    public void test_jvm_overrides_default_boolean_properties() {
        System.setProperty(Constants.JVM_CONDUCTOR_AUTO_WEBVIEW, Boolean.FALSE.toString());
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.autoWebView())
                .isFalse();
    }

    @Test
    public void test_default_int_properties() {
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.timeout())
                .isEqualTo(15);
    }

    @Test
    public void test_config_override_default_int_properties() {
        LocomotiveConfig config = new LocomotiveConfig(testConfig, defaultProperties);
        Assertions.assertThat(config.timeout())
                .isEqualTo(10);
    }

    @Test
    public void test_jvm_overrides_test_config_int_properties() {
        System.setProperty(Constants.JVM_CONDUCTOR_TIMEOUT, String.valueOf(20));
        LocomotiveConfig config = new LocomotiveConfig(testConfig, null);
        Assertions.assertThat(config.timeout())
                .isEqualTo(20);
    }

    @Test
    public void test_jvm_overrides_default_int_properties() {
        System.setProperty(Constants.JVM_CONDUCTOR_TIMEOUT, String.valueOf(20));
        LocomotiveConfig config = new LocomotiveConfig(null, defaultProperties);
        Assertions.assertThat(config.timeout())
                .isEqualTo(20);
    }

    @Test
    public void test_default_timeout() {
        LocomotiveConfig config = new LocomotiveConfig(null, null);
        Assertions.assertThat(config.timeout())
                .isEqualTo(LocomotiveConfig.DEFAULT_MAX_TIMEOUT);
    }

    @Test
    public void test_default_retries() {
        LocomotiveConfig config = new LocomotiveConfig(null, null);
        Assertions.assertThat(config.retries())
                .isEqualTo(LocomotiveConfig.DEFAULT_MAX_RETRIES);
    }

    @Test
    public void test_apk_full_path() {
        Config androidConfig = mock(Config.class);
        when(androidConfig.apk()).thenReturn("/apps/android.apk");
        when(androidConfig.platformName()).thenReturn(Platform.ANDROID);

        LocomotiveConfig config = new LocomotiveConfig(androidConfig, null);
        Assertions.assertThat(config.getAppFullPath())
                .isEqualTo(System.getProperty("user.dir") + "/apps/android.apk");
    }

    @Test
    public void test_ipa_full_path() {
        Config iosConfig = mock(Config.class);
        when(iosConfig.ipa()).thenReturn("/apps/QAInternal.ipa");
        when(iosConfig.platformName()).thenReturn(Platform.IOS);

        LocomotiveConfig config = new LocomotiveConfig(iosConfig, null);
        Assertions.assertThat(config.getAppFullPath())
                .isEqualTo(System.getProperty("user.dir") + "/apps/QAInternal.ipa");
    }

    @Test
    public void test_platform_none_throws_on_full_path() {
        Config mockConfig = mock(Config.class);
        when(mockConfig.platformName()).thenReturn(Platform.NONE);

        final LocomotiveConfig config = new LocomotiveConfig(mockConfig, null);
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                config.getAppFullPath();
            }
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
