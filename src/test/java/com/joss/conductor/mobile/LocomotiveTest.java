package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.IOSDeviceUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.swing.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created on 9/14/16.
 */
public class LocomotiveTest {

    private AppiumDriver mockDriver;
    private LocomotiveConfig androidConfig;
    private LocomotiveConfig iosConfig;

    @Before
    public void setup() {
        mockDriver = mock(AppiumDriver.class);
        androidConfig = mock(LocomotiveConfig.class);
        when(androidConfig.platformName()).thenReturn(Platform.ANDROID);
        when(androidConfig.udid()).thenReturn("qwerty");
        when(androidConfig.appPackageName()).thenReturn("com.joss.conductor.mobile");
        when(androidConfig.orientation()).thenReturn("vertical");
        when(androidConfig.platformVersion()).thenReturn("6.0");
        when(androidConfig.deviceName()).thenReturn("Pixelated Nexus");
        when(androidConfig.getAppFullPath()).thenReturn("/full/path/to/android.apk");
        when(androidConfig.autoAcceptAlerts()).thenReturn(true);

        iosConfig = mock(LocomotiveConfig.class);
        when(iosConfig.platformName()).thenReturn(Platform.IOS);
        when(iosConfig.appPackageName()).thenReturn("com.joss.conductor.mobile");
        when(iosConfig.orientation()).thenReturn("vertical");
        when(iosConfig.platformVersion()).thenReturn("10.0.0");
        when(iosConfig.deviceName()).thenReturn("Bravest Auxless Phone");
        when(iosConfig.getAppFullPath()).thenReturn("/full/path/to/ios.ipa");
        when(iosConfig.autoAcceptAlerts()).thenReturn(true);
    }

    @Test
    public void test_building_android_capabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.UDID, "qwerty");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Pixelated Nexus");
        capabilities.setCapability(MobileCapabilityType.APP, "/full/path/to/android.apk");
        capabilities.setCapability(MobileCapabilityType.ORIENTATION, "vertical");
        Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        Assertions.assertThat(locomotive.buildCapabilities(androidConfig))
                .isEqualToComparingFieldByField(capabilities);
    }

    @Test
    public void test_building_ios_capabilities_no_devices() {
        when(iosConfig.udid()).thenReturn("qwerty");

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.UDID, "qwerty");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Bravest Auxless Phone");
        capabilities.setCapability(MobileCapabilityType.APP, "/full/path/to/ios.ipa");
        capabilities.setCapability(MobileCapabilityType.ORIENTATION, "vertical");
        capabilities.setCapability(Constants.AUTO_ACCEPT_ALERTS, true);
        Locomotive locomotive = new Locomotive(iosConfig, mockDriver);

        Assertions.assertThat(locomotive.buildCapabilities(iosConfig))
                .isEqualToComparingFieldByField(capabilities);
    }

    @Test
    public void test_building_ios_capabilities_with_devices() {
        when(iosConfig.udid()).thenReturn("");
        IOSDeviceUtil util = mock(IOSDeviceUtil.class);
        when(util.getDeviceName("1234")).thenReturn("Puddy");

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.UDID, "1234");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Puddy");
        capabilities.setCapability(MobileCapabilityType.APP, "/full/path/to/ios.ipa");
        capabilities.setCapability(MobileCapabilityType.ORIENTATION, "vertical");
        capabilities.setCapability(Constants.AUTO_ACCEPT_ALERTS, true);

        List<String> devices = Arrays.asList("1234", "2345");

        Locomotive locomotive = new Locomotive(iosConfig, mockDriver);
        locomotive.setIosDeviceUtil(util);

        Assertions.assertThat(locomotive.buildCapabilities(iosConfig, devices))
                .isEqualToComparingFieldByField(capabilities);
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
        LocomotiveConfig config = mock(LocomotiveConfig.class);
        when(config.retries()).thenReturn(numberOfRetries);

        final By id = mock(By.class);
        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList());
        final Locomotive locomotive = new Locomotive(config, mockDriver);

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
        LocomotiveConfig config = mock(LocomotiveConfig.class);
        when(config.retries()).thenReturn(numberOfRetries);

        WebElement foundElement = mock(WebElement.class);
        By id = mock(By.class);

        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(foundElement));
        when(mockDriver.findElement(id)).thenReturn(foundElement);
        Locomotive locomotive = new Locomotive(config, mockDriver);

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
        LocomotiveConfig config = mock(LocomotiveConfig.class);
        when(config.retries()).thenReturn(numberOfRetries);

        final By id = mock(By.class);
        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList());
        final Locomotive locomotive = new Locomotive(config, mockDriver);

        Assertions.assertThat(locomotive.isPresentWait(id))
                .isEqualTo(false);
        verify(mockDriver, times(numberOfRetries + 1)) // First attempt to find elements plus 5 retries
                .findElements(id);
    }

    @Test
    public void test_is_present_wait_retries_and_find_item() {
        int numberOfRetries = 5;
        LocomotiveConfig config = mock(LocomotiveConfig.class);
        when(config.retries()).thenReturn(numberOfRetries);

        WebElement foundElement = mock(WebElement.class);
        By id = mock(By.class);

        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(foundElement));
        when(mockDriver.findElement(id)).thenReturn(foundElement);
        Locomotive locomotive = new Locomotive(config, mockDriver);

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

    @Test
    public void test_auto_accept_alerts_are_on_compatibilities_ios() {
        Locomotive locomotive = new Locomotive(iosConfig, mockDriver);
        Assertions.assertThat(locomotive.buildCapabilities(iosConfig).getCapability(Constants.AUTO_ACCEPT_ALERTS))
                .isEqualTo(true);
    }

    @Test
    public void test_auto_accept_alerts_are_not_on_compatibilities_android() {
        Locomotive locomotive = new Locomotive(androidConfig, mockDriver);
        Assertions.assertThat(locomotive.buildCapabilities(androidConfig).getCapability(Constants.AUTO_ACCEPT_ALERTS))
                .isNull();
    }

}
