package com.joss.conductor.mobile;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.swing.assertions.Assertions;
import org.mockito.ArgumentCaptor;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static com.joss.conductor.mobile.MockTestUtil.*;

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
        androidConfig.setTimeout(0);

        when(mockDriver.findElements(id)).thenReturn(Collections.singletonList(foundElement));
        when(mockDriver.findElement(id)).thenReturn(foundElement);
        Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        Assertions.assertThat(locomotive.waitForElement(id))
                .isEqualTo(foundElement);
        verify(mockDriver, times(2))
                .findElements(id);
    }

    @Test
    public void test_wait_for_ele_retries_and_fail() {
        int numberOfRetries = 5;
        iosConfig.setRetries(numberOfRetries);
        iosConfig.setTimeout(0);

        final By id = mock(By.class);
        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList());
        final Locomotive locomotive = new Locomotive(iosConfig, mockDriver);

        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            public void call() throws Throwable {
                locomotive.waitForElement(id);
            }
        }).isInstanceOf(AssertionError.class);
        // First attempt to find elements plus 5 retries + one added from the driver
        verify(mockDriver, times(numberOfRetries + 2))
                .findElements(id);
    }

    @Test
    public void test_wait_for_ele_retries_and_find_item() {
        int numberOfRetries = 5;
        iosConfig.setRetries(numberOfRetries);
        iosConfig.setTimeout(0);

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
        when(foundElement.isDisplayed()).thenReturn(true);

        when(mockDriver.findElements(id)).thenReturn(Collections.singletonList(foundElement));
        Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        Assertions.assertThat(locomotive.isPresentWait(id))
                .isEqualTo(true);
        verify(mockDriver, times(2))
                .findElements(id);
    }

    @Test
    public void test_is_present_wait_retries_and_fail() {
        int numberOfRetries = 5;
        iosConfig.setRetries(numberOfRetries);
        iosConfig.setTimeout(0);

        final By id = mock(By.class);
        when(mockDriver.findElements(id)).thenReturn(Collections.emptyList());
        final Locomotive locomotive = new Locomotive(iosConfig, mockDriver);

        Assertions.assertThat(locomotive.isPresentWait(id))
                .isEqualTo(false);
        // First attempt to find elements plus 5 retries, plus one from the driver
        verify(mockDriver, times(numberOfRetries + 2))
                .findElements(id);
    }

    @Test
    public void test_is_present_wait_retries_and_find_item() {
        iosConfig.setRetries(5);
        iosConfig.setTimeout(0);

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

    private Map<String, List<Object>> getTouchActionParameters(TouchAction action)
    {
        try {
            Method method = TouchAction.class.getDeclaredMethod("getParameters");
            method.setAccessible(true);
            return (Map)method.invoke(action);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void assertThatActionMatches(TouchAction actual, TouchAction expected) {
        Map<String, List<Object>> actualParameters = getTouchActionParameters(actual);
        Map<String, List<Object>> expectedParameters = getTouchActionParameters(expected);

        assertThat((Map<String, List<Object>>)actualParameters, matchesEntriesIn((Map<String, List<Object>>)expectedParameters));
    }

    private void initMockDriverSizes() {
        initMockDriverSizes(null);
    }

    private void initMockDriverSizes(WebElement mockElement) {
        if(mockElement != null) {
            when(mockElement.getLocation()).thenReturn(new Point(0, 0));
            when(mockElement.getSize()).thenReturn(new Dimension(10, 10));
        }

        WebDriver.Window window = mock(WebDriver.Window.class);
        when(window.getSize()).thenReturn(new Dimension(100, 100));

        WebDriver.Options options = mock(WebDriver.Options.class);
        when(options.window()).thenReturn(window);

        when(mockDriver.manage()).thenReturn(options);
    }

    @Test
    public void test_perform_swipe_center_down() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeCenter(SwipeElementDirection.DOWN);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(50, 50)
                        .waitAction(Duration.ofMillis(2000)).moveTo(50, 75)
                        .release());
    }

    @Test
    public void test_perform_swipe_center_down_long() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeCenterLong(SwipeElementDirection.DOWN);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(50, 50)
                        .waitAction(Duration.ofMillis(2000)).moveTo(50, 99)
                        .release());
    }

    @Test
    public void test_perform_swipe_center_left() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeCenter(SwipeElementDirection.LEFT);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(50, 50)
                        .waitAction(Duration.ofMillis(2000)).moveTo(25, 50)
                        .release());
    }

    @Test
    public void test_perform_swipe_center_left_long() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeCenterLong(SwipeElementDirection.LEFT);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(50, 50)
                        .waitAction(Duration.ofMillis(2000)).moveTo(1, 50)
                        .release());
    }

    @Test
    public void test_perform_swipe_center_up() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeCenter(SwipeElementDirection.UP);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(50, 50)
                        .waitAction(Duration.ofMillis(2000)).moveTo(50, 25)
                        .release());
    }

    @Test
    public void test_perform_swipe_center_up_long() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeCenterLong(SwipeElementDirection.UP);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(50, 50)
                        .waitAction(Duration.ofMillis(2000)).moveTo(50, 1)
                        .release());
    }

    @Test
    public void test_perform_swipe_center_right() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeCenter(SwipeElementDirection.RIGHT);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(50, 50)
                        .waitAction(Duration.ofMillis(2000)).moveTo(75, 50)
                        .release());
    }

    @Test
    public void test_perform_swipe_center_right_long() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeCenterLong(SwipeElementDirection.RIGHT);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(50, 50)
                        .waitAction(Duration.ofMillis(2000)).moveTo(99, 50)
                        .release());
    }

    @Test
    public void test_perform_swipe_none_asserts() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

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
    public void test_perform_swipe_on_element_down()  {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipe(SwipeElementDirection.DOWN, element);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(5, 5)
                        .waitAction(Duration.ofMillis(2000)).moveTo(5, 30)
                        .release());
    }

    @Test
    public void test_perform_swipe_on_element_down_long() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeLong(SwipeElementDirection.DOWN, element);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(5, 5)
                        .waitAction(Duration.ofMillis(2000)).moveTo(5, 55)
                        .release());
    }

    @Test
    public void test_perform_swipe_on_element_left()  {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipe(SwipeElementDirection.LEFT, element);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(5, 5)
                        .waitAction(Duration.ofMillis(2000)).moveTo(1, 5)
                        .release());
    }

    @Test
    public void test_perform_swipe_on_element_left_long() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeLong(SwipeElementDirection.LEFT, element);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(5, 5)
                        .waitAction(Duration.ofMillis(2000)).moveTo(1, 5)
                        .release());
    }

    @Test
    public void test_perform_swipe_on_element_up()  {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipe(SwipeElementDirection.UP, element);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(5, 5)
                        .waitAction(Duration.ofMillis(2000)).moveTo(5, 1)
                        .release());
    }

    @Test
    public void test_perform_swipe_on_element_up_long() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeLong(SwipeElementDirection.UP, element);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(5, 5)
                        .waitAction(Duration.ofMillis(2000)).moveTo(5, 1)
                        .release());
    }

    @Test
    public void test_perform_swipe_on_element_right()  {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipe(SwipeElementDirection.RIGHT, element);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(5, 5)
                        .waitAction(Duration.ofMillis(2000)).moveTo(30, 5)
                        .release());
    }

    @Test
    public void test_perform_swipe_on_element_right_long() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        final Locomotive locomotive = new Locomotive(androidConfig, mockDriver);

        locomotive.swipeLong(SwipeElementDirection.RIGHT, element);
        ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
        verify(mockDriver, times(1))
                .performTouchAction(touchCapture.capture());
        assertThatActionMatches(touchCapture.getValue(),
                new TouchAction(mockDriver).press(5, 5)
                        .waitAction(Duration.ofMillis(2000)).moveTo(55, 5)
                        .release());
    }
}
