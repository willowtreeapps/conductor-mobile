package com.joss.conductor.mobile;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.assertj.swing.assertions.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.joss.conductor.mobile.MockTestUtil.matchesEntriesIn;
import static com.joss.conductor.mobile.SwipeElementDirection.DOWN;
import static io.appium.java_client.touch.WaitOptions.waitOptions;
import static io.appium.java_client.touch.offset.PointOption.point;
import static java.time.Duration.ofMillis;
import static org.hamcrest.MatcherAssert.assertThat;
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
        androidConfig = new ConductorConfig("/test_yaml/android_full.yaml");
        iosConfig = new ConductorConfig("/test_yaml/ios_full.yaml");

        mockDriver = mock(AppiumDriver.class);
        when(mockDriver.getSessionId()).thenReturn(new SessionId("123456789"));
    }

    @Test
    public void test_building_android_capabilities() {
        String nul = null;

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.UDID, "qwerty");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Pixelated Nexus");
        capabilities.setCapability(MobileCapabilityType.APP, "/full/path/to/android.apk");
        capabilities.setCapability(MobileCapabilityType.ORIENTATION, "PORTRAIT");
        capabilities.setCapability("autoGrantPermissions", true);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, false);
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, true);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "6.0");
        capabilities.setCapability(AndroidMobileCapabilityType.AVD, "Nexus 13");
        capabilities.setCapability(AndroidMobileCapabilityType.AVD_ARGS, "-no-window");
        capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, "LaunchActivity");
        capabilities.setCapability(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY, "HomeActivity");
        capabilities.setCapability(AndroidMobileCapabilityType.INTENT_CATEGORY, "android.intent.category.LEANBACK_LAUNCHER");
        capabilities.setCapability("xcodeOrgId", nul);
        capabilities.setCapability("xcodeSigningId", nul);
        capabilities.setCapability("waitForQuiescence", nul);
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "600");
        capabilities.setCapability("idleTimeout", "600");
        capabilities.setCapability("simpleIsVisibleCheck", true);
        capabilities.setCapability(MobileCapabilityType.APPIUM_VERSION, "1.13.0");

        Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

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
        capabilities.setCapability("waitForQuiescence", true);
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, "600");
        capabilities.setCapability("idleTimeout", "600");
        capabilities.setCapability("simpleIsVisibleCheck", true);
        Locomotive locomotive = new Locomotive()
                .setConfiguration(iosConfig)
                .setAppiumDriver(mockDriver);

        Assertions.assertThat(locomotive.buildCapabilities(iosConfig))
                .isEqualToIgnoringNullFields(capabilities);
    }

    @Test
    public void test_custom_capabilities() {
        ConductorConfig config = new ConductorConfig("/test_yaml/android_defaults_custom_caps.yaml");
        Locomotive locomotive = new Locomotive()
                .setConfiguration(config)
                .setAppiumDriver(mockDriver);

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
    public void test_is_present_wait_found_on_first_try() {
        By id = mock(By.class);
        WebElement foundElement = mock(WebElement.class);
        when(foundElement.isDisplayed()).thenReturn(true);

        when(mockDriver.findElements(id)).thenReturn(Collections.singletonList(foundElement));
        Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

        Assertions.assertThat(locomotive.isPresentWait(id))
                .isEqualTo(true);
        verify(mockDriver, times(1))
                .findElements(id);
    }

    @Test
    public void test_get_center_web_element() {
        WebElement element = mock(WebElement.class);
        when(element.getLocation()).thenReturn(new Point(50, 0));
        when(element.getSize()).thenReturn(new Dimension(10, 10));

        Point center = new Point(55, 5);
        Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

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
        Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

        Assertions.assertThat(locomotive.getCenter(/*webElement=*/null))
                .isEqualToComparingFieldByField(center);
    }

    private Map<String, List<Object>> getTouchActionParameters(TouchAction action) {
        try {
            Method method = TouchAction.class.getDeclaredMethod("getParameters");
            method.setAccessible(true);
            return (Map) method.invoke(action);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void assertThatActionMatches(TouchAction actual, TouchAction expected) {
        Map<String, List<Object>> actualParameters = getTouchActionParameters(actual);
        Map<String, List<Object>> expectedParameters = getTouchActionParameters(expected);

        assertThat(actualParameters, matchesEntriesIn(expectedParameters));
    }

    private void initMockDriverSizes() {
        initMockDriverSizes(null);
    }

    private void initMockDriverSizes(WebElement mockElement) {
        if (mockElement != null) {
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

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(50, 75), new Point(0, 25)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCenter(DOWN);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(50, 50))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }

    }

    @Test
    public void test_perform_long_press_swipe_center_down() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(50, 75), new Point(0, 25)};

        for (int i = 0; i < configs.length; i++) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.longPressSwipeCenter(DOWN);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).longPress(point(50, 50))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }

    }

    @Test
    public void test_perform_swipe_with_custom_coordinates() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(20, 20), new Point(19, 19)};

        for (int i = 0; i < configs.length; i++) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipe(new Point(1, 1), new Point(20, 20));
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(1, 1))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void startAppiumSessionCounter() {
        ConductorConfig customConfig = new ConductorConfig("/test_yaml/android_full.yaml");
        // cause startAppiumSession to retry 4 times
        customConfig.setStartSessionRetries(4);

        // spy on the config to count invocations
        ConductorConfig spy = Mockito.spy(customConfig);

        final Locomotive locomotive = new Locomotive()
                .setConfiguration(spy);

        // run the method under test
        try {
            locomotive.startAppiumSession(1);
            assertThat("Expected startAppiumSession() has failed", false);
        } catch (WebDriverException e) {
            assertThat("Verify startAppiumSession() has failed", true);
        }

        // expected 4 retries, verified by making sure the spy has been called 4 times.
        verify(spy, times(4)).isLocal();
    }

    @Test
    public void test_perform_swipe_center_down_long() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(50, 99), new Point(0, 49)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCenterLong(DOWN);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(50, 50))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_center_left() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(25, 50), new Point(-25, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCenter(SwipeElementDirection.LEFT);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(50, 50))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_center_left_long() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(1, 50), new Point(-49, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCenterLong(SwipeElementDirection.LEFT);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(50, 50))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_center_up() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(50, 25), new Point(0, -25)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCenter(SwipeElementDirection.UP);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(50, 50))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_center_up_long() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(50, 1), new Point(0, -49)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCenterLong(SwipeElementDirection.UP);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(50, 50))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_center_right() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(75, 50), new Point(25, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCenter(SwipeElementDirection.RIGHT);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(50, 50))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_center_right_long() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(99, 50), new Point(49, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCenterLong(SwipeElementDirection.RIGHT);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(50, 50))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_none_asserts() {
        initMockDriverSizes();

        final Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

        Assertions.assertThatThrownBy(() -> locomotive.swipeCenter(SwipeElementDirection.NONE)).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(() -> locomotive.swipeCenterLong(SwipeElementDirection.NONE)).isInstanceOf(IllegalArgumentException.class);

        // Swipe @null
        Assertions.assertThatThrownBy(() -> locomotive.swipeCenter(null)).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(() -> locomotive.swipeCenterLong(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void test_perform_corner_swipe_bottom_right() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(90, 40), new Point(0, -50)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerLong(ScreenCorner.BOTTOM_RIGHT, SwipeElementDirection.UP, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(90, 90))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_corner_swipe_long_bottom_right() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(90, 1), new Point(0, -89)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerSuperLong(ScreenCorner.BOTTOM_RIGHT, SwipeElementDirection.UP, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(90, 90))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_corner_swipe_bottom_left() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(10, 40), new Point(0, -50)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerLong(ScreenCorner.BOTTOM_LEFT, SwipeElementDirection.UP, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(10, 90))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_corner_swipe_long_bottom_left() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(10, 1), new Point(0, -89)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerSuperLong(ScreenCorner.BOTTOM_LEFT, SwipeElementDirection.UP, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(10, 90))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_corner_swipe_top_right() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(90, 60), new Point(0, 50)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerLong(ScreenCorner.TOP_RIGHT, DOWN, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(90, 10))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_corner_swipe_long_top_right() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(90, 99), new Point(0, 89)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerSuperLong(ScreenCorner.TOP_RIGHT, DOWN, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(90, 10))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_corner_swipe_top_left() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(10, 60), new Point(0, 50)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerLong(ScreenCorner.TOP_LEFT, DOWN, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(10, 10))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_corner_swipe_long_top_left() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(10, 99), new Point(0, 89)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerSuperLong(ScreenCorner.TOP_LEFT, DOWN, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(10, 10))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_corner_swipe_right_top_left() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(60, 10), new Point(50, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerLong(ScreenCorner.TOP_LEFT, SwipeElementDirection.RIGHT, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(10, 10))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_corner_swipe_left_top_right() {
        initMockDriverSizes();

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(40, 10), new Point(-50, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeCornerLong(ScreenCorner.TOP_RIGHT, SwipeElementDirection.LEFT, 100);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(90, 10))
                            .waitAction(waitOptions(ofMillis(100)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_on_element_down() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(5, 30), new Point(0, 25)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipe(DOWN, element);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(5, 5))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_on_element_down_long() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(5, 55), new Point(0, 50)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeLong(DOWN, element);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(5, 5))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_on_element_left() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(1, 5), new Point(-4, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipe(SwipeElementDirection.LEFT, element);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(5, 5))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_on_element_left_long() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(1, 5), new Point(-4, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeLong(SwipeElementDirection.LEFT, element);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(5, 5))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_on_element_up() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(5, 1), new Point(0, -4)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipe(SwipeElementDirection.UP, element);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(5, 5))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_on_element_up_long() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(5, 1), new Point(0, -4)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeLong(SwipeElementDirection.UP, element);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(5, 5))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_on_element_right() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(30, 5), new Point(25, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipe(SwipeElementDirection.RIGHT, element);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(5, 5))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_perform_swipe_on_element_right_long() {
        final WebElement element = mock(WebElement.class);
        initMockDriverSizes(element);

        ConductorConfig[] configs = {androidConfig, iosConfig};
        Point[] moveTo = {new Point(55, 5), new Point(50, 0)};

        for (int i = 0; i < 2; ++i) {
            final Locomotive locomotive = new Locomotive()
                    .setConfiguration(configs[i])
                    .setAppiumDriver(mockDriver);

            locomotive.swipeLong(SwipeElementDirection.RIGHT, element);
            ArgumentCaptor<TouchAction> touchCapture = ArgumentCaptor.forClass(TouchAction.class);
            verify(mockDriver, times(i + 1))
                    .performTouchAction(touchCapture.capture());
            assertThatActionMatches(touchCapture.getValue(),
                    new TouchAction(mockDriver).press(point(5, 5))
                            .waitAction(waitOptions(ofMillis(2000)))
                            .moveTo(point(moveTo[i].x, moveTo[i].y))
                            .release());
        }
    }

    @Test
    public void test_getText_returns_element_text() {
        final WebElement element = mock(WebElement.class);

        final Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

        when(element.getText()).thenReturn("string");
        assertThat("Expected element text to return \"ElementText\" but it does not.", locomotive.getText(element).equals("string"));
    }

    @Test(expectedExceptions = NoSuchElementException.class, expectedExceptionsMessageRegExp = "Unable to find element: .*" )
    public void test_getText_returns_exception() {
        final WebElement element = mock(WebElement.class);

        final Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

        when(element.getText()).thenThrow(NoSuchElementException.class);
        locomotive.getText(element);
    }

    @Test(expectedExceptions = NoSuchElementException.class, expectedExceptionsMessageRegExp = "Unable to find element: .*")
    public void test_setText_returns_exception() {
        final WebElement element = mock(WebElement.class);

        final Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

        doThrow(NoSuchElementException.class).when(element).sendKeys(anyString());

        locomotive.setText(element, "text");
    }

    @Test(expectedExceptions = NoSuchElementException.class, expectedExceptionsMessageRegExp = "Unable to find element: .*")
    public void test_click_returns_exception() {
        final WebElement element = mock(WebElement.class);

        final Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

        doThrow(NoSuchElementException.class).when(element).click();
        locomotive.click(element);
    }

    @Test
    public void test_getAttribute_returns_attribute() {
        final WebElement element = mock(WebElement.class);

        final Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

        when(element.getAttribute(anyString())).thenReturn("true");
        assertThat("Error: Expected element attribute of \"visible\" to return \"true\", but it did not.", locomotive.getAttribute(element, "visible").equals("true"));
    }

    @Test(expectedExceptions = NoSuchElementException.class, expectedExceptionsMessageRegExp = "Unable to find element: .*")
    public void test_getAttribute_returns_exception() {
        final WebElement element = mock(WebElement.class);

        final Locomotive locomotive = new Locomotive()
                .setConfiguration(androidConfig)
                .setAppiumDriver(mockDriver);

        when(element.getAttribute(anyString())).thenThrow(NoSuchElementException.class);
        locomotive.getAttribute(element, anyString());
    }
}
