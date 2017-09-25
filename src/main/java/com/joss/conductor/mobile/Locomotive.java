package com.joss.conductor.mobile;

import com.google.common.base.Strings;
import com.joss.conductor.mobile.util.PageUtil;
import com.joss.conductor.mobile.util.PropertiesUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.swing.dependency.jsr305.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 8/10/16.
 */
@Listeners(TestListener.class)
public class Locomotive extends Watchman implements Conductor<Locomotive> {

    private static final float SWIPE_DISTANCE = 0.25f;
    private static final float SWIPE_DISTANCE_LONG = 0.50f;
    private static final int SWIPE_DURATION_MILLIS = 2000;

    public LocomotiveConfig configuration;
    public AppiumDriver driver;

    private Map<String, String> vars = new HashMap<String, String>();

    @Rule
    public TestRule watchman = this;

    public Locomotive getLocomotive() {
        return this;
    }

    public Locomotive() {
    }

    /**
     * Constructor for Unit Tests
     */
    public Locomotive(LocomotiveConfig configuration, AppiumDriver driver) {
        init(configuration, driver);
    }

    @Before
    @BeforeMethod (alwaysRun = true)
    public void init() {
        Properties props = PropertiesUtil.getDefaultProperties(this);
        Config testConfiguration = this.getClass().getAnnotation(Config.class);
        init(props, testConfiguration);
    }

    @AfterMethod (alwaysRun = true)
    public void quit() {
        driver.quit();
    }

    private void init(Properties properties, Config testConfig) {
        init(new LocomotiveConfig(testConfig, properties), /*AppiumDriver=*/null);
    }

    private void init(LocomotiveConfig configuration, AppiumDriver driver) {
        this.configuration = configuration;
        if (driver != null) {
            this.driver = driver;
        } else {
            boolean isLocal = StringUtils.isEmpty(configuration.hub());
            URL url = getUrl(isLocal);
            DesiredCapabilities capabilities = onCapabilitiesCreated(getCapabilities(configuration));

            AppiumServiceBuilder builder = new AppiumServiceBuilder()
                    .withArgument(GeneralServerFlag.LOG_LEVEL, configuration.logLevel().equals("")
                            ? "debug"
                            : configuration.logLevel());

            switch (configuration.platformName()) {
                case ANDROID:
                    this.driver = isLocal
                            ? new AndroidDriver(builder, capabilities)
                            : new AndroidDriver(url, capabilities);

                    break;
                case IOS:
                    this.driver = isLocal
                            ? new IOSDriver(builder, capabilities)
                            : new IOSDriver(url, capabilities);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown platform: " + configuration.platformName());
            }
        }
    }

    @Nullable
    private URL getUrl(boolean isLocal) {
        URL url = null;
        if (!isLocal) {
            try {
                url = new URL(configuration.hub());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    protected DesiredCapabilities onCapabilitiesCreated(DesiredCapabilities desiredCapabilities) {
        return desiredCapabilities;
    }

    private DesiredCapabilities getCapabilities(LocomotiveConfig configuration) {
        DesiredCapabilities capabilities;
        switch (configuration.platformName()) {
            case ANDROID:
            case IOS:
                capabilities = buildCapabilities(configuration);
                break;
            default:
                throw new IllegalArgumentException("Unknown platform: " + configuration.platformName());
        }

        // If deviceName is empty replace it with something
        // noinspection Since15
        if (capabilities.getCapability(MobileCapabilityType.DEVICE_NAME).toString().isEmpty()) {
            capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Empty Device Name");
        }

        return capabilities;
    }

    public DesiredCapabilities buildCapabilities(LocomotiveConfig config) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.UDID, config.udid());
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, config.deviceName());
        capabilities.setCapability(MobileCapabilityType.APP, config.getAppFullPath());
        capabilities.setCapability(MobileCapabilityType.ORIENTATION, config.orientation());
        capabilities.setCapability("autoGrantPermissions", config.autoGrantPermissions());
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, config.fullReset());
        capabilities.setCapability(MobileCapabilityType.NO_RESET, config.noReset());
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, config.platformVersion());
        capabilities.setCapability("xcodeSigningId", config.xcodeSigningId());
        capabilities.setCapability("xcodeOrgId", config.xcodeOrgId());
        capabilities.setCapability(AndroidMobileCapabilityType.AVD, config.avd());
        capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, config.appActivity());
        capabilities.setCapability(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY, config.appWaitActivity());

        if (StringUtils.isNotEmpty(config.automationName())) {
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, config.automationName());
        }

        if (config.platformName() == Platform.IOS) {
            capabilities.setCapability(Constants.AUTO_ACCEPT_ALERTS, config.autoAcceptAlerts());
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
            System.out.println("WaitForElement: Eat exception thrown waiting for condition");
        }

        int size = driver.findElements(by).size();

        if (size == 0) {
            int attempts = 1;
            while (attempts <= configuration.retries()) {
                try {
                    Thread.sleep(1000); // sleep for 1 second.
                } catch (Exception x) {
                    Assert.fail("Failed due to an exception during Thread.sleep!");
                    x.printStackTrace();
                }

                size = driver.findElements(by).size();
                if (size > 0) {
                    break;
                }
                attempts++;
            }
            if (size == 0) {
                Assert.fail(String.format("Could not find %s after %d attempts",
                        by.toString(),
                        configuration.retries()));
            }
        }

        if (size > 1) {
            System.err.println("WARN: There are more than 1 " + by.toString() + " 's!");
        }

        return driver.findElement(by);
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
        return driver.findElements(by).size() > 0;
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
            System.err.println(newLine + newLine + "----     WARNING: METHOD DID NOT FIND ELEMENT  ----" + newLine);

            if (stackTraceElements != null) {

                int traceSize = stackTraceElements.length;

                if ((traceSize) > 4) {

                    try {
                        System.err.print(stackTraceElements[1] + newLine);
                        System.err.println(stackTraceElements[2]);
                        System.err.println(stackTraceElements[3] + newLine);
                    } catch (ArrayIndexOutOfBoundsException exception) {
                        System.err.print(exception);
                    }
                }
            }
            System.err.println(newLine + newLine + "----     WARNING: ELEMENT NOT PRESENT  ---- "+ newLine + e.toString() + newLine + newLine);
        }

        int size = driver.findElements(by).size();

        if (size == 0) {
            int attempts = 1;
            while (attempts <= configuration.retries()) {
                try {
                    Thread.sleep(1000); // sleep for 1 second.
                } catch (Exception x) {
                    Assertions.fail(x.getMessage(), x);
                }

                size = driver.findElements(by).size();
                if (size > 0) {
                    break;
                }
                attempts++;
            }
        }

        if (size > 1) {
            System.err.println("WARN: There are more than 1 " + by.toString() + " 's!");
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
            driver.hideKeyboard();
        } catch (WebDriverException e) {
            System.err.println("WARN:" + e.getMessage());
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

        Dimension screen = driver.manage().window().getSize();
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
        driver.swipe(from.getX(), from.getY(), to.getX(), to.getY(), SWIPE_DURATION_MILLIS);
        return this;
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
            x = driver.manage().window().getSize().getWidth() / 2;
            y = driver.manage().window().getSize().getHeight() / 2;
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
        return driver.findElements(by);
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
        Assert.assertTrue(driver.getPageSource().contains(text));
        return this;
    }

    public Locomotive validateTextNotPresent(String text) {
        Assert.assertFalse(driver.getPageSource().contains(text));
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
     * Wait for a specific condition (polling every 1s, for MAX_TIMEOUT seconds)
     *
     * @param condition the condition to wait for
     * @return The implementing class for fluency
     */
    public Locomotive waitForCondition(ExpectedCondition<?> condition) {
        return waitForCondition(condition, configuration.timeout());
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
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds, sleepInMillis);
        wait.until(condition);
        return this;
    }
}
