package com.joss.conductor.mobile;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.swing.assertions.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Created on 9/14/16.
 */
public class LocomotiveTest {

    private AppiumDriver mockDriver;
    private ConductorConfig androidConfig;
    private ConductorConfig iosConfig;

    @BeforeMethod
    public void setup() {
        mockDriver = mock(AppiumDriver.class);
        androidConfig = new ConductorConfig("/test_yaml/android_full.yaml");
        iosConfig = new ConductorConfig("/test_yaml/ios_full.yaml");
    }

    @Test
    public void test_building_android_capabilities() {
        String nul = null;

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.UDID, "qwerty");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Pixelated Nexus");
        capabilities.setCapability(MobileCapabilityType.APP, "/full/path/to/android.apk");
        capabilities.setCapability(MobileCapabilityType.ORIENTATION, "vertical");
        capabilities.setCapability("autoGrantPermissions", true);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, false);
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, true);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "6.0");
        capabilities.setCapability(AndroidMobileCapabilityType.AVD, "Nexus 13");
        capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, "LaunchActivity");
        capabilities.setCapability(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY, "HomeActivity");
        capabilities.setCapability(AndroidMobileCapabilityType.INTENT_CATEGORY, "android.intent.category.LEANBACK_LAUNCHER");
        capabilities.setCapability("xcodeOrgId", nul);
        capabilities.setCapability("xcodeSigningId", nul);
        Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        Assertions.assertThat(locomotive.buildCapabilities(androidConfig))
                .isEqualToComparingFieldByField(capabilities);
    }

    @Test
    public void test_building_ios_capabilities_no_devices() {
        iosConfig.setUdid("qwerty");
        String nul = null;

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.UDID, "qwerty");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Bravest Auxless Phone");
        capabilities.setCapability(MobileCapabilityType.APP, "/full/path/to/ios.ipa");
        capabilities.setCapability(MobileCapabilityType.ORIENTATION, "vertical");
        capabilities.setCapability("autoGrantPermissions", false);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, false);
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, false);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "11.0");
        capabilities.setCapability(AndroidMobileCapabilityType.AVD, nul);
        capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, nul);
        capabilities.setCapability(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY, nul);
        capabilities.setCapability(AndroidMobileCapabilityType.INTENT_CATEGORY, nul);
        capabilities.setCapability("xcodeOrgId", "orgId");
        capabilities.setCapability("xcodeSigningId", "signingId");
        Locomotive locomotive = new Locomotive(iosConfig, mockDriver);

        Assertions.assertThat(locomotive.buildCapabilities(iosConfig))
                .isEqualToIgnoringNullFields(capabilities);
    }

    @Test
    public void test_custom_capabilities() {
        ConductorConfig config = new ConductorConfig("/test_yaml/android_defaults_custom_caps.yaml");
        Locomotive locomotive = new Locomotive(config, mockDriver);

        DesiredCapabilities caps = locomotive.buildCapabilities(config);
        Assertions.assertThat(caps.getCapability("foo"))
                .isEqualTo("bar");
        Assertions.assertThat(caps.getCapability("fizz"))
                .isEqualTo("buzz");
        Assertions.assertThat(caps.getCapability(AndroidMobileCapabilityType.APP_ACTIVITY))
                .isEqualTo("com.android.activity");
        Assertions.assertThat(caps.getCapability(MobileCapabilityType.NO_RESET))
                .isEqualTo(false);
    }

    @Test
    public void test_wait_for_elem_found_on_first_try() {
        By id = mock(By.class);
        WebElement foundElement = mock(WebElement.class);

        when(mockDriver.findElements(id)).thenReturn(Collections.singletonList(foundElement));
        when(mockDriver.findElement(id)).thenReturn(foundElement);
        Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        Assertions.assertThat(locomotive.waitForElement(id))
                .isEqualTo(foundElement);
        verify(mockDriver, times(1))
                .findElements(id);
    }

    @Test
    public void test_wait_for_ele_retries_and_fail() {
        int numberOfRetries = 5;
        iosConfig.setRetries(numberOfRetries);

        final By id = mock(By.class);
        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList());
        final Locomotive locomotive = new Locomotive(iosConfig, mockDriver);

        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.waitForElement(id);
            }
        }).isInstanceOf(AssertionError.class);
        verify(mockDriver, times(numberOfRetries + 1)) // First attempt to find elements plus 5 retries
                .findElements(id);
    }

    @Test
    public void test_wait_for_ele_retries_and_find_item() {
        int numberOfRetries = 5;
        iosConfig.setRetries(numberOfRetries);

        WebElement foundElement = mock(WebElement.class);
        By id = mock(By.class);

        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(foundElement));
        when(mockDriver.findElement(id)).thenReturn(foundElement);
        Locomotive locomotive = new Locomotive(iosConfig, mockDriver);

        Assertions.assertThat(locomotive.waitForElement(id))
                .isEqualTo(foundElement);
        verify(mockDriver, times(3)) // Found on it's 3rd attempt
                .findElements(id);
    }

    @Test
    public void test_is_present_wait_found_on_first_try() {
        By id = mock(By.class);
        WebElement foundElement = mock(WebElement.class);

        when(mockDriver.findElements(id)).thenReturn(Collections.singletonList(foundElement));
        when(mockDriver.findElement(id)).thenReturn(foundElement);
        Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        Assertions.assertThat(locomotive.isPresentWait(id))
                .isEqualTo(true);
        verify(mockDriver, times(1))
                .findElements(id);
    }

    @Test
    public void test_is_present_wait_retries_and_fail() {
        int numberOfRetries = 5;
        iosConfig.setRetries(numberOfRetries);

        final By id = mock(By.class);
        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList());
        final Locomotive locomotive = new Locomotive(iosConfig, mockDriver);

        Assertions.assertThat(locomotive.isPresentWait(id))
                .isEqualTo(false);
        verify(mockDriver, times(numberOfRetries + 1)) // First attempt to find elements plus 5 retries
                .findElements(id);
    }

    @Test
    public void test_is_present_wait_retries_and_find_item() {
        int numberOfRetries = 5;
        iosConfig.setRetries(numberOfRetries);

        WebElement foundElement = mock(WebElement.class);
        By id = mock(By.class);

        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(foundElement));
        when(mockDriver.findElement(id)).thenReturn(foundElement);
        Locomotive locomotive = new Locomotive(iosConfig, mockDriver);

        Assertions.assertThat(locomotive.isPresentWait(id))
                .isEqualTo(true);
        verify(mockDriver, times(3)) // Found on it's 3rd attempt
                .findElements(id);
    }

    @Test
    public void test_get_center_web_element() {
        WebElement element = mock(WebElement.class);
        when(element.getLocation()).thenReturn(new Point(50, 0));
        when(element.getSize()).thenReturn(new Dimension(10, 10));

        Point center = new Point(55, 5);
        Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        Assertions.assertThat(locomotive.getCenter(element))
                .isEqualToComparingFieldByField(center);
    }

    @Test
    public void test_get_center_window() {
        WebDriver.Window window = mock(WebDriver.Window.class);
        when(window.getSize()).thenReturn(new Dimension(100, 50));

        WebDriver.Options options = mock(WebDriver.Options.class);
        when(options.window()).thenReturn(window);

        when(mockDriver.manage()).thenReturn(options);

        Point center = new Point(50, 25);
        Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        Assertions.assertThat(locomotive.getCenter(/*webElement=*/null))
                .isEqualToComparingFieldByField(center);
    }

    @Test
    public void test_perform_swipe_center() {
        WebDriver.Window window = mock(WebDriver.Window.class);
        when(window.getSize()).thenReturn(new Dimension(100, 100));

        WebDriver.Options options = mock(WebDriver.Options.class);
        when(options.window()).thenReturn(window);

        when(mockDriver.manage()).thenReturn(options);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        // Swipe Down
        locomotive.swipeCenter(SwipeElementDirection.DOWN);
        locomotive.swipeCenterLong(SwipeElementDirection.DOWN);
        verify(mockDriver, times(1))
                .swipe(50, 50, 50, 75, /*SWIPE_DURATION_MILLS=*/2000);
        verify(mockDriver, times(1))
                .swipe(50, 50, 50, /*(y - 1 to avoid going off screen) y=*/99,
                        /*SWIPE_DURATION_MILLS=*/2000);

        // Swipe Left
        locomotive.swipeCenter(SwipeElementDirection.LEFT);
        locomotive.swipeCenterLong(SwipeElementDirection.LEFT);
        verify(mockDriver, times(1))
                .swipe(50, 50, 25, 50, /*SWIPE_DURATION_MILLS=*/2000);
        verify(mockDriver, times(1))
                .swipe(50, 50, /* (x + 1 to avoid going off screen) x=*/1,
                        50, /*SWIPE_DURATION_MILLS=*/2000);

        // Swipe Up
        locomotive.swipeCenter(SwipeElementDirection.UP);
        locomotive.swipeCenterLong(SwipeElementDirection.UP);
        verify(mockDriver, times(1))
                .swipe(50, 50, 50, 25, /*SWIPE_DURATION_MILLS=*/2000);
        verify(mockDriver, times(1))
                .swipe(50, 50, 50, /*(y + 1 to avoid going off screen) y=*/1,
                        /*SWIPE_DURATION_MILLS=*/2000);

        // Swipe Right
        locomotive.swipeCenter(SwipeElementDirection.RIGHT);
        locomotive.swipeCenterLong(SwipeElementDirection.RIGHT);
        verify(mockDriver, times(1))
                .swipe(50, 50, 75, 50, /*SWIPE_DURATION_MILLS=*/2000);
        verify(mockDriver, times(1))
                .swipe(50, 50, /*(x - 1 to avoid going off screen) x=*/99,
                        50, /*SWIPE_DURATION_MILLS=*/2000);

        // Swipe None
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.swipeCenter(SwipeElementDirection.NONE);
            }
        }).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.swipeCenterLong(SwipeElementDirection.NONE);
            }
        }).isInstanceOf(IllegalArgumentException.class);

        // Swipe @null
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.swipeCenter(null);
            }
        }).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.swipeCenterLong(null);
            }
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void perform_swipe_on_element() {
        final WebElement element = mock(WebElement.class);
        when(element.getLocation()).thenReturn(new Point(0, 0));
        when(element.getSize()).thenReturn(new Dimension(10, 10));

        WebDriver.Window window = mock(WebDriver.Window.class);
        when(window.getSize()).thenReturn(new Dimension(100, 100));

        WebDriver.Options options = mock(WebDriver.Options.class);
        when(options.window()).thenReturn(window);

        when(mockDriver.manage()).thenReturn(options);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        // Swipe Down
        locomotive.swipe(SwipeElementDirection.DOWN, element);
        locomotive.swipeLong(SwipeElementDirection.DOWN, element);
        verify(mockDriver, times(1))
                .swipe(5, 5, 5, 5 + 25, /*SWIPE_DURATION_MILLS=*/2000);
        verify(mockDriver, times(1))
                .swipe(5, 5, 5, 5 + 50, /*SWIPE_DURATION_MILLS=*/2000);

        // Swipe Left
        locomotive.swipe(SwipeElementDirection.LEFT, element);
        locomotive.swipeLong(SwipeElementDirection.LEFT, element);
        verify(mockDriver, times(2))
                .swipe(5, 5, /* x - 25 or 50(+1 if negative or zero) x=*/ 1,
                        5, /*SWIPE_DURATION_MILLS=*/2000);

        // Swipe Up
        locomotive.swipe(SwipeElementDirection.UP, element);
        locomotive.swipeLong(SwipeElementDirection.UP, element);
        verify(mockDriver, times(2))
                .swipe(5, 5, 5, /* y - 25 or 50 (+1 if negative or zero) y=*/1,
                        /*SWIPE_DURATION_MILLS=*/2000);

        // Swipe Right
        locomotive.swipe(SwipeElementDirection.RIGHT, element);
        locomotive.swipeLong(SwipeElementDirection.RIGHT, element);
        verify(mockDriver, times(1))
                .swipe(5, 5, 5 + 25, 5, /*SWIPE_DURATION_MILLS=*/2000);
        verify(mockDriver, times(1))
                .swipe(5, 5, 5 + 50, 5, /*SWIPE_DURATION_MILLS=*/2000);

        // Swipe None
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.swipe(SwipeElementDirection.NONE, element);
            }
        }).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.swipeLong(SwipeElementDirection.NONE, element);
            }
        }).isInstanceOf(IllegalArgumentException.class);

        // Swipe @null
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.swipe(null, element);
            }
        }).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.swipeLong(null, element);
            }
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
