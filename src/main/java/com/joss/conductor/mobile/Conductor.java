package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.PageUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pmw.tinylog.Logger;

import java.util.List;

import static io.appium.java_client.touch.WaitOptions.waitOptions;
import static io.appium.java_client.touch.offset.PointOption.point;
import static java.time.Duration.ofMillis;

public class Conductor {

    public static Conductor getInstance(ConductorConfig config, AppiumDriver driver) {
        return new Conductor(config, driver);
    }

    private static final float SWIPE_DISTANCE = 0.25f;
    private static final float SWIPE_DISTANCE_LONG = 0.50f;
    private static final float SWIPE_DISTANCE_SUPER_LONG = 1.0f;
    private static final int SWIPE_DURATION_MILLIS = 2000;

    private ConductorConfig config;
    private AppiumDriver driver;

    private Conductor(ConductorConfig config, AppiumDriver driver) {
        this.config = config;
        this.driver = driver;
    }

    public ConductorConfig getConfig() {
        return config;
    }

    public AppiumDriver getDriver() {
        return driver;
    }

    /**
     * Method that acts as an arbiter of implicit timeouts of sorts
     */
    public WebElement waitForElement(String id) {
        return waitForElement(PageUtil.buildBy(config, id));
    }

    public WebElement waitForElement(By by) {

        try {
            waitForCondition(ExpectedConditions.not(ExpectedConditions.invisibilityOfElementLocated(by)));
        } catch (Exception e) {
            Logger.info("WaitForElement: Eat exception thrown waiting for condition");
        }

        int size = driver.findElements(by).size();

        if (size == 0) {
            int attempts = 1;
            while (attempts <= config.getRetries()) {
                try {
                    Thread.sleep(1000); // sleep for 1 second.
                } catch (Exception x) {
                    Logger.error("Failed due to an exception during Thread.sleep!");
                    Logger.error(x);
                    return null;
                }

                size = driver.findElements(by).size();
                if (size > 0) {
                    break;
                }
                attempts++;
            }
            if (size == 0) {
                Logger.error(String.format("Could not find %s after %d attempts",
                        by.toString(),
                        config.getRetries()));
                return null;
            }
        }

        if (size > 1) {
            Logger.warn("WARN: There are more than 1 " + by.toString() + " 's!");
        }

        return driver.findElement(by);
    }


    public Conductor click(String id) {
        return click(PageUtil.buildBy(config, id));
    }

    public Conductor click(By by) {
        return click(waitForElement(by));
    }

    public Conductor click(WebElement element) {
        element.click();
        return this;
    }

    public Conductor setText(String id, String text) {
        return setText(PageUtil.buildBy(config, id), text);
    }

    public Conductor setText(By by, String text) {
        return setText(waitForElement(by), text);
    }

    public Conductor setText(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
        return this;
    }

    public boolean isPresent(String id) {
        return isPresent(PageUtil.buildBy(config, id));
    }

    public boolean isPresent(By by) {
        return driver.findElements(by).size() > 0;
    }

    public boolean isPresentWait(String id) {
        return isPresentWait(PageUtil.buildBy(config, id));
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

        int size = driver.findElements(by).size();

        if (size == 0) {
            int attempts = 1;
            while (attempts <= config.getRetries()) {
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
            Logger.error("WARN: There are more than 1 " + by.toString() + " 's!");
        }

        return size > 0;
    }

    public String getText(String id) {
        return getText(PageUtil.buildBy(config, id));
    }

    public String getText(By by) {
        return getText(waitForElement(by));
    }

    public String getText(WebElement element) {
        return element.getText();
    }

    public String getAttribute(String id, String attribute) {
        return getAttribute(PageUtil.buildBy(config, id), attribute);
    }

    public String getAttribute(By by, String attribute) {
        return waitForElement(by).getAttribute(attribute);
    }

    public String getAttribute(WebElement element, String attribute) {
        return element.getAttribute(attribute);
    }

    public Conductor swipeCenter(SwipeElementDirection direction) {
        return performSwipe(direction, /*element=*/null, /*by=*/null, SWIPE_DISTANCE);
    }

    public Conductor swipe(SwipeElementDirection direction, String id) {
        return swipe(direction, PageUtil.buildBy(config, id));
    }

    public Conductor swipe(SwipeElementDirection direction, By by) {
        return swipe(direction, by, SWIPE_DISTANCE);
    }

    public Conductor swipe(SwipeElementDirection direction, WebElement element) {
        return performSwipe(direction, element, /*by=*/null, SWIPE_DISTANCE);
    }

    public Conductor swipeCenterLong(SwipeElementDirection direction) {
        return performSwipe(direction, /*element=*/null, /*by=*/null, SWIPE_DISTANCE_LONG);
    }

    public Conductor swipeCenterSuperLong(SwipeElementDirection direction) {
        return performSwipe(direction, /*element=*/null, /*by=*/null, SWIPE_DISTANCE_SUPER_LONG);
    }

    public Conductor swipeCornerLong(ScreenCorner corner, SwipeElementDirection direction, int duration) {
        return performCornerSwipe(corner, direction, SWIPE_DISTANCE_LONG, duration);
    }

    public Conductor swipeCornerSuperLong(ScreenCorner corner, SwipeElementDirection direction, int duration) {
        return performCornerSwipe(corner, direction, SWIPE_DISTANCE_SUPER_LONG, duration);
    }

    public Conductor swipeLong(SwipeElementDirection direction, String id) {
        return swipeLong(direction, PageUtil.buildBy(config, id));
    }

    public Conductor swipeLong(SwipeElementDirection direction, By by) {
        return swipe(direction, by, SWIPE_DISTANCE_LONG);
    }

    public Conductor swipeLong(SwipeElementDirection direction, WebElement element) {
        return performSwipe(direction, element, /*by=*/null, SWIPE_DISTANCE_LONG);
    }

    public Conductor swipe(SwipeElementDirection direction, By by, float percentage) {
        return performSwipe(direction, /*element=*/null, by, percentage);
    }

    public Conductor swipe(SwipeElementDirection direction, WebElement element, float percentage) {
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

    public Conductor hideKeyboard() {
        try {
            driver.hideKeyboard();
        } catch (WebDriverException e) {
            Logger.error("WARN:" + e.getMessage());
        }
        return this;
    }

    private Conductor performSwipe(SwipeElementDirection direction, WebElement element, By by, float percentage) {
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

        // Appium specifies that TouchAction.moveTo should be relative. iOS implements this correctly, but android
        // does not. As a result we have to check if we're on iOS and perform the relativization manually
        if (config.getPlatformName() == Platform.IOS) {
            to = new Point(to.getX() - from.getX(), to.getY() - from.getY());
        }

        TouchAction swipe = new TouchAction(driver)
                .press(point(from.getX(), from.getY()))
                .waitAction(waitOptions(ofMillis(SWIPE_DURATION_MILLIS)))
                .moveTo(point(to.getX(), to.getY()))
                .release();
        swipe.perform();
        return this;
    }

    private Conductor performCornerSwipe(ScreenCorner corner, SwipeElementDirection direction, float percentage, int duration) {
        Dimension screen = driver.manage().window().getSize();

        final int SCREEN_MARGIN = 10;

        Point from;
        if (corner != null) {
            switch (corner) {
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
        if (direction != null) {
            switch (direction) {
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
        if (config.getPlatformName() == Platform.IOS) {
            to = new Point(to.getX() - from.getX(), to.getY() - from.getY());
        }

        new TouchAction(driver)
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
                element = driver.findElement(by);
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
            x = driver.manage().window().getSize().getWidth() / 2;
            y = driver.manage().window().getSize().getHeight() / 2;
        } else {
            x = element.getLocation().getX() + (element.getSize().getWidth() / 2);
            y = element.getLocation().getY() + (element.getSize().getHeight() / 2);
        }
        return new Point(x, y);
    }

    public List<WebElement> getElements(String id) {
        return getElements(PageUtil.buildBy(config, id));
    }

    public List<WebElement> getElements(By by) {
        waitForElement(by);
        return driver.findElements(by);
    }

    /**
     * Wait for a specific condition (polling every 1s, for MAX_TIMEOUT seconds)
     *
     * @param condition the condition to wait for
     * @return The implementing class for fluency
     */
    public Conductor waitForCondition(ExpectedCondition<?> condition) {
        return waitForCondition(condition, config.getTimeout());
    }

    /**
     * Wait for a specific condition (polling every 1s)
     *
     * @param condition        the condition to wait for
     * @param timeOutInSeconds the timeout in seconds
     * @return The implementing class for fluency
     */
    public Conductor waitForCondition(ExpectedCondition<?> condition, long timeOutInSeconds) {
        return waitForCondition(condition, timeOutInSeconds, 1000); // poll every second
    }

    public Conductor waitForCondition(ExpectedCondition<?> condition, long timeOutInSeconds, long sleepInMillis) {
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds, sleepInMillis);
        wait.until(condition);
        return this;
    }


    public Conductor waitUntilNotPresent(String id) {
        return waitForCondition(ExpectedConditions.invisibilityOfElementLocated(PageUtil.buildBy(config, id)));
    }

}
