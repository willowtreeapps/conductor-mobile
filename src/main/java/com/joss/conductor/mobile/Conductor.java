package com.joss.conductor.mobile;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Created on 8/19/16.
 */
public interface Conductor<Test> {

    WebElement waitForElement(String id);
    WebElement waitForElement(By by);

    /*
        Actions
     */
    /**
     * Click an element.
     * @param id/by The element to click.
     * @return The implementing class for fluency
     */
    Test click(String id);
    Test click(By by);
    Test click(WebElement element);

    /**
     * Clears the text from a text field, and sets it.
     * @param id/by The element to set the text of.
     * @param text The text that the element will have.
     * @return The implementing class for fluency
     */
    Test setText(String id, String text);
    Test setText(By by, String text);
    Test setText(WebElement element, String text);

    /**
     * Checks if the element is present or not.<br>
     * @param id/by The element
     * @return <i>this method is not meant to be used fluently.</i><br><br>.
     * Returns <code>true</code> if the element is present. and <code>false</code> if it's not.
     */
    boolean isPresent(String id);
    boolean isPresent(By by);

    /**
     * Method to continuously checks if the element is present or not for 5 seconds (default).<br>
     * @param id/by The element
     * @return <i>this method is not meant to be used fluently.</i><br><br>.
     * Returns <code>true</code> if the element is present. and <code>false</code> if it's not.
     */
    boolean isPresentWait(String id);
    boolean isPresentWait(By by);

    /**
     * Get the text of an element.
     * <blockquote>This is a consolidated method that works on input's, as select boxes, and fetches the value rather than the innerHTMl.</blockquote>
     * @param id/by The element
     * @return The implementing class for fluency
     */
    String getText(String id);
    String getText(By by);
    String getText(WebElement element);

    /**
     * Get an attribute of an element
     * @param id/by The element to get the attribute from
     * @param attribute The attribute to get
     * @return The implementing class for fluency
     */
    String getAttribute(String id, String attribute);
    String getAttribute(By by, String attribute);
    String getAttribute(WebElement element, String attribute);

    /**
     * Swipe on specified direction from center 25 percent of the screen
     * @return The implementing class for fluency
     */
    Test swipeCenter(SwipeElementDirection direction);

    /**
     * Swipe on specified direction from center 50 percent of the screen
     * @param direction The direction to swipe to
     * @return The implementing class for fluency
     */
    Test swipeCenterLong(SwipeElementDirection direction);


    Test swipeCornerLong(ScreenCorner corner, SwipeElementDirection direction, int duration);
    Test swipeCornerSuperLong(ScreenCorner corner, SwipeElementDirection direction, int duration);

    /**
     * Swipe on specified direction from element 25 percent of the screen
     * @param id/by/element The element to swipe from.
     * @param direction The direction to swipe to
     * @return The implementing class for fluency
     */
    Test swipe(SwipeElementDirection direction, String id);
    Test swipe(SwipeElementDirection direction, By by);
    Test swipe(SwipeElementDirection direction, WebElement element);

    /**
     * Swipe on specified direction from element 50 percent of the screen
     * @param id/by/element The element to swipe from.
     * @param direction The direction to swipe to
     * @return The implementing class for fluency
     */
    Test swipeLong(SwipeElementDirection direction, String id);
    Test swipeLong(SwipeElementDirection direction, By by);
    Test swipeLong(SwipeElementDirection direction, WebElement element);

    /**
     * Swipe on specified direction from element, if element is null, swipe from center.
     * @param by The element to swipe from
     * @param direction The direction to swipe to
     * @param percentage The distance to perform the swipe 0.0 to 1.0
     * @return
     */
    Test swipe(SwipeElementDirection direction, By by, float percentage);
    Test swipe(SwipeElementDirection direction, WebElement element, float percentage);

    /**
     * Hides keyboard if present
     * @return
     */
    Test hideKeyboard();

    /*
        Validations
     */
    /**
     * Validates that an element is present.
     * @param id/by The element
     * @return The implementing class for fluency
     */
    Test validatePresent(String id);
    Test validatePresent(By by);

    /**
     * Validates that an element is not present.
     * @param id/by The element
     * @return The implementing class for fluency
     */
    Test validateNotPresent(String id);
    Test validateNotPresent(By by);

    /**
     * Validate that the text of an element is correct.
     * @param id/by/element The element to validate the text of.
     * @param text The text the element should have.
     * @return The implementing class for fluency
     */
    Test validateText(String id, String text);
    Test validateText(By by, String text);
    Test validateText(WebElement element, String text);

    /**
     * Validate that the text of an element is correct, ignoring case.
     * @param id/by/element The element to validate the text of.
     * @param text The text the element should have.
     * @return The implementing class for fluency
     */
    Test validateTextIgnoreCase(String id, String text);
    Test validateTextIgnoreCase(By by, String text);
    Test validateTextIgnoreCase(WebElement element, String text);

    /**
     * Validate that the text of an element is not matching text.
     * @param id/by/element The element to validate the text of.
     * @param text The text the element should not have.
     * @return The implementing class for fluency
     */
    Test validateTextNot(String id, String text);
    Test validateTextNot(By by, String text);
    Test validateTextNot(WebElement element, String text);

    /**
     * Validate that the text of an element is not matching text, ignoring case.
     * @param id/by/element The element to validate the text of.
     * @param text The text the element should not have.
     * @return The implementing class for fluency
     */
    Test validateTextNotIgnoreCase(String id, String text);
    Test validateTextNotIgnoreCase(By by, String text);
    Test validateTextNotIgnoreCase(WebElement element, String text);

    /**
     * Validate that text is present somewhere on the page.
     * @param text The text to ensure is on the page.
     * @return The implementing class for fluency
     */
    Test validateTextPresent(String text);

    /**
     * Validate that some text is nowhere on the page.
     * @param text The text to ensure is not on the page.
     * @return The implementing class for fluency
     */
    Test validateTextNotPresent(String text);

    /**
     * Validates an attribute of an element.<br><br>
     * Example:<br>
     * <blockquote>
     * {@literal <input type="text" id="test" />}
     * <br><br>
     * <code>.validateAttribute("input#test", "type", "text") // validates that the "type" attribute equals "test"</code>
     * </blockquote>
     * @param id/by/element The element
     * @param attr The attribute you'd like to validate
     * @param regex What the attribute <b>should</b> be.  (this method supports regex)
     * @return The implementing class for fluency
     */
    Test validateAttribute(String id, String attr, String regex);
    Test validateAttribute(By by, String attr, String regex);
    Test validateAttribute(WebElement element, String attr, String regex);

    /**
     * Validates that a specific condition is true
     * @param condition The condition that is expected to be true
     * @return The implementing class for fluency
     */
    Test validateTrue(boolean condition);

    /**
     * Validates that a specific condition is false
     * @param condition The condition that is expected to be false
     * @return The implementing class for fluency
     */
    Test validateFalse(boolean condition);

    /*
        Test collections
     */
    /**
     * Put a variable in the data warehouse.
     * @param key The key to put.
     * @param value The value to put.
     * @return The implementing class for fluency
     */
    Test store(String key, String value);

    /**
     * Get a variable from the data warehouse.<br><br>
     * If the key is not set, then use {@link #get(String, String)}
     * @param key The key to fetch.
     * @return The implementing class for fluency
     */
    String get(String key);

    /**
     * Get a variable from the data warehouse.
     * @param key The key to fetch.
     * @param defaultValue The value to return if the variable is not set.
     * @return The implementing class for fluency
     */
    String get(String key, String defaultValue);

    /*
        Waits
     */

    /**
     * Wait for a specific condition before continuing
     * @param condition the condition to wait for
     * @param timeOutInSeconds how long to wait
     * @param sleepInMillis the delay between waits
     * @return The implementing class for fluency
     */
    Test waitForCondition(ExpectedCondition<?> condition, long timeOutInSeconds, long sleepInMillis);
}
