package com.joss.conductor.mobile;

import com.google.common.base.Strings;
import com.joss.conductor.mobile.util.PageUtil;
import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.CommandExecutionHelper;
import io.appium.java_client.MobileCommand;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.PerformsTouchID;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pmw.tinylog.LogEntry;
import org.pmw.tinylog.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.appium.java_client.touch.WaitOptions.waitOptions;
import static io.appium.java_client.touch.offset.PointOption.point;
import static java.time.Duration.ofMillis;

/**
 * Created on 8/10/16.
 */
@Listeners({TestListener.class, SauceLabsListener.class})
public class Locomotive extends Watchman implements Conductor<Locomotive>, SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {

    private static final float SWIPE_DISTANCE = 0.25f;
    private static final float SWIPE_DISTANCE_LONG = 0.50f;
    private static final float SWIPE_DISTANCE_SUPER_LONG = 1.0f;
    private static final int SWIPE_DURATION_MILLIS = 2000;

    private ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    private ThreadLocal<String> sessionId = new ThreadLocal<>();

    public ConductorConfig configuration;
    private Map<String, String> vars = new HashMap<>();
    private String testMethodName;

    @Rule
    public TestRule watchman = this;

    @Rule
    public TestName testNameRule = new TestName();

    public Locomotive getLocomotive() {
        return this;
    }

    public Locomotive() {
    }

    public AppiumDriver getAppiumDriver() {
        return driver.get();
    }

    public Locomotive setAppiumDriver(AppiumDriver d) {
        driver.set(d);
        return this;
    }

    public Locomotive setConfiguration(ConductorConfig configuration) {
        this.configuration = configuration;
        return this;
    }

    @Before
    public void init() {
        // For jUnit get the method name from a test rule.
        this.testMethodName = testNameRule.getMethodName();
        initialize();
    }

    @BeforeMethod(alwaysRun = true)
    public void init(Method method) {
        // For testNG get the method name from an injected dependency.
        this.testMethodName = method.getName();
        initialize();
    }

    @AfterMethod(alwaysRun = true)
    public void quit() {
        try {
            getAppiumDriver().quit();
            driver.remove();
        } catch (org.openqa.selenium.WebDriverException exception) {
            Logger.warn(exception,"WebDriverException occurred during quit method");
        }
    }

    private void initialize() {
        if (this.configuration == null) {
            this.configuration = new ConductorConfig();
        }

        startAppiumSession(1);

        // Set session ID after driver has been initialized
        SessionId id = getAppiumDriver().getSessionId();
        sessionId.set(id.toString());

        // TODO: Added to support biometrics on android until java-client PR #473 is pulled in
        MobileCommand.commandRepository.put("fingerPrint",
                MobileCommand.postC("/session/:sessionId/appium/device/finger_print"));
    }

    void startAppiumSession(int startCounter) {
        if (getAppiumDriver() != null && getAppiumDriver().getSessionId() != null) {
            // session is already active -> terminal condition
            return;
        }

        if (startCounter > configuration.getStartSessionRetries()) {
            // maximum amount of retries reached
            throw new WebDriverException(
                    "Could not start Appium Session");
        }

        // start a new session
        try {
            URL                 hub          = configuration.getHub();
            DesiredCapabilities capabilities = onCapabilitiesCreated(getCapabilities(configuration));

            AppiumServiceBuilder builder = new AppiumServiceBuilder()
                    .withArgument(GeneralServerFlag.LOG_LEVEL, "debug");

            switch (configuration.getPlatformName()) {
                case ANDROID:
                    setAppiumDriver(configuration.isLocal()
                            ? new AndroidDriver(builder, capabilities)
                            : new AndroidDriver(hub, capabilities));
                    break;
                case IOS:
                    setAppiumDriver(configuration.isLocal()
                            ? new IOSDriver(builder, capabilities)
                            : new IOSDriver(hub, capabilities));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown platform: " + configuration.getPlatformName());
            }
        } catch (WebDriverException exception) {
            Logger.error(exception, "Received an exception while trying to start Appium session");
        }

        // recursive call to retry if necessary
        startAppiumSession(startCounter + 1);
    }

    protected DesiredCapabilities onCapabilitiesCreated(DesiredCapabilities desiredCapabilities) {
        return desiredCapabilities;
    }

    private DesiredCapabilities getCapabilities(ConductorConfig configuration) {
        DesiredCapabilities capabilities;
        switch (configuration.getPlatformName()) {
            case ANDROID:
            case IOS:
                capabilities = buildCapabilities(configuration);
                break;
            default:
                throw new IllegalArgumentException("Unknown platform: " + configuration.getPlatformName());
        }

        // If deviceName is empty replace it with something
        // noinspection Since15
        if (capabilities.getCapability(MobileCapabilityType.DEVICE_NAME).toString().isEmpty()) {
            capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Empty Device Name");
        }

        return capabilities;
    }

    public DesiredCapabilities buildCapabilities(ConductorConfig config) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
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

        return capabilities;
    }

    /**
     * Method that acts as an arbiter of implicit timeouts of sorts
     */
    public WebElement waitForElement(String id) {
        return waitForElement(PageUtil.buildBy(configuration, id));
    }

    public WebElement waitForElement(By by) {

        try {
            waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)));
        } catch (Exception e) {
            Logger.info("WaitForElement: Eat exception thrown waiting for condition");
        }

        int size = getAppiumDriver().findElements(by).size();

        if (size == 0) {
            int attempts = 1;
            while (attempts <= configuration.getRetries()) {
                try {
                    Thread.sleep(1000); // sleep for 1 second.
                } catch (Exception x) {
                    Assert.fail("Failed due to an exception during Thread.sleep!");
                    Logger.error(x);
                }

                size = getAppiumDriver().findElements(by).size();
                if (size > 0) {
                    break;
                }
                attempts++;
            }
            if (size == 0) {
                Assert.fail(String.format("Could not find %s after %d attempts",
                        by.toString(),
                        configuration.getRetries()));
            }
        }

        if (size > 1) {
            Logger.error("WARN: There are more than 1 " + by.toString() + " 's!");
        }

        return getAppiumDriver().findElement(by);
    }


    public Locomotive click(String id) {
        return click(PageUtil.buildBy(configuration, id));
    }

    public Locomotive click(By by) {
        return click(waitForElement(by));
    }

    public Locomotive click(WebElement element) {
        element.click();
        return this;
    }

    public Locomotive setText(String id, String text) {
        return setText(PageUtil.buildBy(configuration, id), text);
    }

    public Locomotive setText(By by, String text) {
        return setText(waitForElement(by), text);
    }

    public Locomotive setText(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
        return this;
    }

    public boolean isPresent(String id) {
        return isPresent(PageUtil.buildBy(configuration, id));
    }

    public boolean isPresent(By by) {
        return getAppiumDriver().findElements(by).size() > 0;
    }

    public boolean isPresentWait(String id) {
        return isPresentWait(PageUtil.buildBy(configuration, id));
    }

    public boolean isPresentWait(By by) {

        //Line Separator Variable for formatting output
        String newLine = System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
        //Array of stacktrace elements to output
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        try {
            waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)));

        } catch (Exception e) {
            Logger.error(newLine + newLine + "----     WARNING: METHOD DID NOT FIND ELEMENT  ----" + newLine);

            if (stackTraceElements != null) {

                int traceSize = stackTraceElements.length >= 3 ? 3 : stackTraceElements.length;

                try {

                    for (int i = 0; i < traceSize; i++) {

                        if (stackTraceElements[i] != null) {
                            Logger.error(stackTraceElements[i] + newLine);
                        }
                    }

                } catch (ArrayIndexOutOfBoundsException exception) {
                    Logger.error(exception);
                }
            }


            Logger.error(newLine + newLine + "----     WARNING: ELEMENT NOT PRESENT  ---- " + newLine + e.toString() + newLine + newLine);
        }

        int size = getAppiumDriver().findElements(by).size();

        if (size == 0) {
            int attempts = 1;
            while (attempts <= configuration.getRetries()) {
                try {
                    Thread.sleep(1000); // sleep for 1 second.
                } catch (Exception x) {
                    Assertions.fail(x.getMessage(), x);
                }

                size = getAppiumDriver().findElements(by).size();
                if (size > 0) {
                    break;
                }
                attempts++;
            }
        }

        if (size > 1) {
            Logger.error("WARN: There are more than 1 " + by.toString() + " 's!");
        }

        return size > 0;
    }

    public String getText(String id) {
        return getText(PageUtil.buildBy(configuration, id));
    }

    public String getText(By by) {
        return getText(waitForElement(by));
    }

    public String getText(WebElement element) {
        return element.getText();
    }

    public String getAttribute(String id, String attribute) {
        return getAttribute(PageUtil.buildBy(configuration, id), attribute);
    }

    public String getAttribute(By by, String attribute) {
        return waitForElement(by).getAttribute(attribute);
    }

    public String getAttribute(WebElement element, String attribute) {
        return element.getAttribute(attribute);
    }

    public Locomotive swipeCenter(SwipeElementDirection direction) {
        return performSwipe(direction, /*element=*/null, /*by=*/null, SWIPE_DISTANCE);
    }

    public Locomotive swipe(SwipeElementDirection direction, String id) {
        return swipe(direction, PageUtil.buildBy(configuration, id));
    }

    public Locomotive swipe(SwipeElementDirection direction, By by) {
        return swipe(direction, by, SWIPE_DISTANCE);
    }

    public Locomotive swipe(SwipeElementDirection direction, WebElement element) {
        return performSwipe(direction, element, /*by=*/null, SWIPE_DISTANCE);
    }

    public Locomotive swipeCenterLong(SwipeElementDirection direction) {
        return performSwipe(direction, /*element=*/null, /*by=*/null, SWIPE_DISTANCE_LONG);
    }

    public Locomotive swipeCenterSuperLong(SwipeElementDirection direction) {
        return performSwipe(direction, /*element=*/null, /*by=*/null, SWIPE_DISTANCE_SUPER_LONG);
    }

    public Locomotive swipeCornerLong(ScreenCorner corner, SwipeElementDirection direction, int duration) {
        return performCornerSwipe(corner, direction, SWIPE_DISTANCE_LONG, duration);
    }

    public Locomotive swipeCornerSuperLong(ScreenCorner corner, SwipeElementDirection direction, int duration) {
        return performCornerSwipe(corner, direction, SWIPE_DISTANCE_SUPER_LONG, duration);
    }

    public Locomotive swipeLong(SwipeElementDirection direction, String id) {
        return swipeLong(direction, PageUtil.buildBy(configuration, id));
    }

    public Locomotive swipeLong(SwipeElementDirection direction, By by) {
        return swipe(direction, by, SWIPE_DISTANCE_LONG);
    }

    public Locomotive swipeLong(SwipeElementDirection direction, WebElement element) {
        return performSwipe(direction, element, /*by=*/null, SWIPE_DISTANCE_LONG);
    }

    public Locomotive swipe(SwipeElementDirection direction, By by, float percentage) {
        return performSwipe(direction, /*element=*/null, by, percentage);
    }

    public Locomotive swipe(SwipeElementDirection direction, WebElement element, float percentage) {
        return performSwipe(direction, element, /*by=*/null, percentage);
    }

    public void swipeDown() {
        swipeCenterLong(SwipeElementDirection.UP);
    }

    public void swipeDown(int times) {
        for (int i = 0; i < times; i++) {
            swipeCenterLong(SwipeElementDirection.UP);
        }
    }

    public void swipeUp() {
        swipeCenterLong(SwipeElementDirection.DOWN);
    }

    public void swipeUp(int times) {
        for (int i = 0; i < times; i++) {
            swipeCenterLong(SwipeElementDirection.DOWN);
        }
    }

    public void swipeRight() {
        swipeCenterLong(SwipeElementDirection.LEFT);
    }

    public void swipeRight(int times) {
        for (int i = 0; i < times; i++) {
            swipeCenterLong(SwipeElementDirection.LEFT);
        }
    }

    public void swipeLeft() {
        swipeCenterLong(SwipeElementDirection.RIGHT);
    }

    public void swipeLeft(int times) {
        for (int i = 0; i < times; i++) {
            swipeCenterLong(SwipeElementDirection.RIGHT);
        }
    }

    public Locomotive hideKeyboard() {
        try {
            getAppiumDriver().hideKeyboard();
        } catch (WebDriverException e) {
            Logger.error("WARN:" + e.getMessage());
        }
        return this;
    }

    private Locomotive performSwipe(SwipeElementDirection direction, WebElement element, By by, float percentage) {
        Point from;
        if (element != null) {
            from = getCenter(element);
        } else if (by != null) {
            from = getCenter(waitForElement(by));
        } else {
            from = getCenter(/*element=*/null);
        }

        Dimension screen = getAppiumDriver().manage().window().getSize();
        Point to = null;
        if (direction != null) {
            switch (direction) {
                case UP:
                    int toYUp = (int) (from.getY() - (screen.getHeight() * percentage));
                    toYUp = toYUp <= 0 ? 1 : toYUp; // toYUp cannot be less than 0
                    to = new Point(from.getX(), toYUp);
                    break;
                case RIGHT:
                    int toXRight = (int) (from.getX() + (screen.getWidth() * percentage));
                    toXRight = toXRight >= screen.getWidth() ? screen.getWidth() - 1 : toXRight; // toXRight cannot be longer than screen width
                    to = new Point(toXRight, from.getY());
                    break;
                case DOWN:
                    int toYDown = (int) (from.getY() + (screen.getHeight() * percentage));
                    toYDown = toYDown >= screen.getHeight() ? screen.getHeight() - 1 : toYDown; // toYDown cannot be longer than screen height
                    to = new Point(from.getX(), toYDown);
                    break;
                case LEFT:
                    int toXLeft = (int) (from.getX() - (screen.getWidth() * percentage));
                    toXLeft = toXLeft <= 0 ? 1 : toXLeft; // toXLeft cannot be less than 0
                    to = new Point(toXLeft, from.getY());
                    break;
                default:
                    throw new IllegalArgumentException("Swipe Direction not specified: " + direction.name());
            }
        } else {
            throw new IllegalArgumentException("Swipe Direction not specified");
        }

        // Appium specifies that TouchAction.moveTo should be relative. iOS implements this correctly, but android
        // does not. As a result we have to check if we're on iOS and perform the relativization manually
        if(configuration.getPlatformName() == Platform.IOS) {
            to = new Point(to.getX() - from.getX(), to.getY() - from.getY());
        }

        TouchAction swipe = new TouchAction(getAppiumDriver())
                .press(point(from.getX(), from.getY()))
                .waitAction(waitOptions(ofMillis(SWIPE_DURATION_MILLIS)))
                .moveTo(point(to.getX(), to.getY()))
                .release();
        swipe.perform();
        return this;
    }
  
    private Locomotive performCornerSwipe(ScreenCorner corner, SwipeElementDirection direction, float percentage, int duration) {
        Dimension screen = getAppiumDriver().manage().window().getSize();

        final int SCREEN_MARGIN = 10;

        Point from;
        if(corner != null) {
            switch(corner) {
                case TOP_LEFT:
                    from = new Point(SCREEN_MARGIN, SCREEN_MARGIN);
                    break;
                case TOP_RIGHT:
                    from = new Point(screen.getWidth() - SCREEN_MARGIN, SCREEN_MARGIN);
                    break;
                case BOTTOM_LEFT:
                    from = new Point(SCREEN_MARGIN, screen.getHeight() - SCREEN_MARGIN);
                    break;
                case BOTTOM_RIGHT:
                    from = new Point(screen.getWidth() - SCREEN_MARGIN, screen.getHeight() - SCREEN_MARGIN);
                    break;
                default:
                    throw new IllegalArgumentException("Corner not specified: " + corner.name());
            }
        } else {
            throw new IllegalArgumentException("Corner not specified");
        }

        Point to;
        if(direction != null) {
            switch(direction) {
                case UP:
                    int toYUp = (int) (from.getY() - (screen.getHeight() * percentage));
                    toYUp = toYUp <= 0 ? 1 : toYUp;
                    to = new Point(from.getX(), toYUp);
                    break;
                case RIGHT:
                    int toXRight = (int) (from.getX() + (screen.getWidth() * percentage));
                    toXRight = toXRight >= screen.getWidth() ? screen.getWidth() - 1 : toXRight; // toXRight cannot be longer than screen width;
                    to = new Point(toXRight, from.getY());
                    break;
                case DOWN:
                    int toYDown = (int) (from.getY() + (screen.getWidth() * percentage));
                    toYDown = toYDown >= screen.getHeight() ? screen.getHeight() - 1 : toYDown; // toYDown cannot be longer than screen height;
                    to = new Point(from.getX(), toYDown);
                    break;
                case LEFT:
                    int toXLeft = (int) (from.getX() - (screen.getWidth() * percentage));
                    toXLeft = toXLeft <= 0 ? 1 : toXLeft; // toXLeft cannot be less than 0
                    to = new Point(toXLeft, from.getY());
                    break;
                default:
                    throw new IllegalArgumentException("Swipe Direction not specified: " + direction.name());

            }
        } else {
            throw new IllegalArgumentException("Swipe Direction not specified");
        }

        // Appium specifies that TouchAction.moveTo should be relative. iOS implements this correctly, but android
        // does not. As a result we have to check if we're on iOS and perform the relativization manually
        if(configuration.getPlatformName() == Platform.IOS) {
            to = new Point(to.getX() - from.getX(), to.getY() - from.getY());
        }

        new TouchAction(getAppiumDriver())
                .press(point(from.getX(), from.getY()))
                .waitAction(waitOptions(ofMillis(duration)))
                .moveTo(point(to.getX(), to.getY()))
                .release()
                .perform();
        return this;
    }

    public WebElement swipeTo(SwipeElementDirection direction, By by, int attempts) {
        WebElement element;
        for (int i = 0; i < attempts; i++) {
            swipeCenterLong(direction);
            try {
                element = getAppiumDriver().findElement(by);
                // element was found, check for visibility
                if (element.isDisplayed()) {
                    // element is in view, exit the loop
                    return element;
                }
                // element was not visible, continue scrolling
            } catch (WebDriverException exception) {
                // element could not be found, continue scrolling
            }
        }
        // element could not be found or was not visible, return null
        Logger.warn("Element " + by.toString() + " does not exist!");
        return null;
    }

    public WebElement swipeTo(By by) {
        SwipeElementDirection s = SwipeElementDirection.UP;
        int attempts = 3;

        return swipeTo(s, by, attempts);
    }

    public WebElement swipeTo(SwipeElementDirection direction, By by) {
        int attempts = 3;

        return swipeTo(direction, by, attempts);
    }

    public WebElement swipeTo(SwipeElementDirection direction, String id, int attempts) {
        return swipeTo(direction, By.id(id), attempts);
    }

    /**
     * Get center point of element, if element is null return center of screen
     *
     * @param element The element to get the center point form
     * @return Point centered on the provided element or screen.
     */

    public Point getCenter(WebElement element) {
        int x, y;
        if (element == null) {
            x = getAppiumDriver().manage().window().getSize().getWidth() / 2;
            y = getAppiumDriver().manage().window().getSize().getHeight() / 2;
        } else {
            x = element.getLocation().getX() + (element.getSize().getWidth() / 2);
            y = element.getLocation().getY() + (element.getSize().getHeight() / 2);
        }
        return new Point(x, y);
    }

    public List<WebElement> getElements(String id) {
        return getElements(PageUtil.buildBy(configuration, id));
    }

    public List<WebElement> getElements(By by) {
        waitForElement(by);
        return getAppiumDriver().findElements(by);
    }

    /**
     * Validation Functions for Testing
     */
    public Locomotive validatePresent(String id) {
        return validatePresent(PageUtil.buildBy(configuration, id));
    }

    public Locomotive validatePresent(By by) {
        waitForElement(by);
        Assert.assertTrue("Element " + by.toString() + " does not exist!", isPresent(by));
        return this;
    }

    public Locomotive validateNotPresent(String id) {
        return validateNotPresent(PageUtil.buildBy(configuration, id));
    }

    public Locomotive validateNotPresent(By by) {
        Assert.assertFalse("Element " + by.toString() + " exists!", isPresent(by));
        return this;
    }

    public Locomotive validateText(String id, String text) {
        return validateText(PageUtil.buildBy(configuration, id), text);
    }

    public Locomotive validateTextIgnoreCase(String id, String text) {
        return validateTextIgnoreCase(PageUtil.buildBy(configuration, id), text);
    }

    public Locomotive validateTextIgnoreCase(By by, String text) {
        return validateTextIgnoreCase(waitForElement(by), text);
    }

    public Locomotive validateTextIgnoreCase(WebElement element, String text) {
        String actual = getText(element);
        Assert.assertTrue(String.format("Text does not match! [expected: %s] [actual: %s]", text, actual),
                text.equalsIgnoreCase(actual));
        return this;
    }

    public Locomotive validateText(By by, String text) {
        return validateText(waitForElement(by), text);
    }

    public Locomotive validateText(WebElement element, String text) {
        String actual = getText(element);
        Assert.assertTrue(String.format("Text does not match! [expected: %s] [actual: %s]", text, actual),
                text.equals(actual));
        return this;
    }

    public Locomotive validateTextNot(String id, String text) {
        return validateTextNot(PageUtil.buildBy(configuration, id), text);
    }

    public Locomotive validateTextNotIgnoreCase(String id, String text) {
        return validateTextNotIgnoreCase(PageUtil.buildBy(configuration, id), text);
    }

    public Locomotive validateTextNotIgnoreCase(By by, String text) {
        return validateTextNotIgnoreCase(waitForElement(by), text);
    }

    public Locomotive validateTextNotIgnoreCase(WebElement element, String text) {
        String actual = getText(element);
        Assert.assertFalse(String.format("Text matches! [expected: %s] [actual: %s]", text, actual),
                text.equalsIgnoreCase(actual));
        return this;
    }

    public Locomotive validateTextNot(By by, String text) {
        return validateTextNot(waitForElement(by), text);
    }

    public Locomotive validateTextNot(WebElement element, String text) {
        String actual = getText(element);
        Assert.assertFalse(String.format("Text matches! [expected: %s] [actual: %s]", text, actual),
                text.equals(actual));
        return this;
    }

    public Locomotive validateTextPresent(String text) {
        Assert.assertTrue(getAppiumDriver().getPageSource().contains(text));
        return this;
    }

    public Locomotive validateTextNotPresent(String text) {
        Assert.assertFalse(getAppiumDriver().getPageSource().contains(text));
        return this;
    }

    public Locomotive validateAttribute(String id, String attr, String regex) {
        return validateAttribute(PageUtil.buildBy(configuration, id), attr, regex);
    }

    public Locomotive validateAttribute(By by, String attr, String regex) {
        return validateAttribute(waitForElement(by), attr, regex);
    }

    public Locomotive validateAttribute(WebElement element, String attr, String regex) {
        String actual = null;
        try {
            actual = element.getAttribute(attr);
            if (actual.equals(regex)) return this; // test passes.
        } catch (NoSuchElementException e) {
            Assert.fail("No such element [" + element.toString() + "] exists.");
        } catch (Exception x) {
            Assert.fail("Cannot validate an attribute if an element doesn't have it!");
        }

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(actual);

        Assert.assertTrue(
                String.format("Attribute doesn't match! [Selector: %s] [Attribute: %s] [Desired value: %s] [Actual value: %s]",
                        element.toString(),
                        attr,
                        regex,
                        actual
                ),
                m.find());

        return this;
    }

    public Locomotive validateTrue(boolean condition) {
        Assert.assertTrue(condition);
        return this;
    }

    public Locomotive validateFalse(boolean condition) {
        Assert.assertFalse(condition);
        return this;
    }

    public Locomotive store(String key, String value) {
        vars.put(key, value);
        return this;
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, String defaultValue) {
        return Strings.isNullOrEmpty(vars.get(key))
                ? defaultValue
                : vars.get(key);
    }

    /**
     * Enroll biometrics. This command is ignored on Android.
     *
     * @return The implementing class for fluency
     */
    public Locomotive enrollBiometrics(int id) {
        switch (configuration.getPlatformName()) {
            case ANDROID:
                // Don't do anything for now
                break;

            case IOS:
                PerformsTouchID performsTouchID = (PerformsTouchID)driver;
                performsTouchID.toggleTouchIDEnrollment(true);
                break;

            case NONE:
                break;
        }

        return this;
    }

    /**
     * Perform a biometric scan, either forcing a match (iOS) or by supplying the id of an enrolled
     * fingerprint (Android)
     *
     * @param match Whether or not the finger should match. This parameter is ignored on Android
     * @param id The id of the enrolled finger. This parameter is ignored on iOS
     * @return The implementing class for fluency
     */
    public Locomotive performBiometric(boolean match, int id) {

        switch (configuration.getPlatformName()) {
            case ANDROID:
                // TODO: Restructure when the Java-client supports biometrics (PR #473 on appium/java-client)
                Map.Entry<String, Map<String, ?>> paramMap = new AbstractMap.SimpleEntry<>("fingerPrint",
                        MobileCommand.prepareArguments("fingerprintId", id));
                CommandExecutionHelper.execute(getAppiumDriver(), paramMap);
                break;

            case IOS:
                PerformsTouchID performsTouchID = (PerformsTouchID) getAppiumDriver();
                performsTouchID.performTouchID(match);
                break;

            case NONE:
                break;
        }

        return this;
    }

    /**
     * Wait for a specific condition (polling every 1s, for MAX_TIMEOUT seconds)
     *
     * @param condition the condition to wait for
     * @return The implementing class for fluency
     */
    public Locomotive waitForCondition(ExpectedCondition<?> condition) {
        return waitForCondition(condition, configuration.getTimeout());
    }

    /**
     * Wait for a specific condition (polling every 1s)
     *
     * @param condition        the condition to wait for
     * @param timeOutInSeconds the timeout in seconds
     * @return The implementing class for fluency
     */
    public Locomotive waitForCondition(ExpectedCondition<?> condition, long timeOutInSeconds) {
        return waitForCondition(condition, timeOutInSeconds, 1000); // poll every second
    }

    public Locomotive waitForCondition(ExpectedCondition<?> condition, long timeOutInSeconds, long sleepInMillis) {
        WebDriverWait wait = new WebDriverWait(getAppiumDriver(), timeOutInSeconds, sleepInMillis);
        wait.until(condition);
        return this;
    }


    public Locomotive waitUntilNotPresent(String id) {
        return waitForCondition(ExpectedConditions.invisibilityOfElementLocated(PageUtil.buildBy(configuration, id)));
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    @Override
    public String getSessionId() {
        return sessionId.get();
    }

    @Override
    public SauceOnDemandAuthentication getAuthentication() {
        return configuration.getSauceAuthentication(configuration.getSauceUserName(), configuration.getSauceAccessKey());
    }
}
