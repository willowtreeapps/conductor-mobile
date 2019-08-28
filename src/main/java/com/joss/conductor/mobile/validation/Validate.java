package com.joss.conductor.mobile.validation;

import com.joss.conductor.mobile.Conductor;
import com.joss.conductor.mobile.ConductorConfig;
import com.joss.conductor.mobile.util.PageUtil;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validate {

    public Validate with(Conductor conductor) {
        return new Validate(conductor);
    }

    private Conductor conductor;
    private By by;

    private Validate(Conductor conductor) {
        this.conductor = conductor;
    }

    public void fail(String message) {
        if (message == null) {
            throw new AssertionError();
        }
        throw new AssertionError(message);
    }


    public Validate that(By by) {
        this.by = by;
        return this;
    }

    public Validate that(String id) {
        this.by = PageUtil.buildBy(conductor.getConfig(), id);
        return this;
    }

    public Validate andThat(By by) {
        return that(by);
    }

    public Validate andThat(String id) {
        return that(id);
    }

    public Validate isPresent() {
        if (conductor.waitForElement(by) == null) {
            fail("Could not find element: " + by.toString());
        }
        return this;
    }

    public Validate isNotPresent() {
        if (conductor.waitForElement(by) != null) {
            fail("Element " + by.toString() + " exists");
        }
        return this;
    }

    public Validate equalsTo(String text) {
        String actual = conductor.getText(by);
        if (!actual.equals(text)) {
            fail(String.format("Text does not match! [expected: %s] [actual: %s]", text, actual));
        }
        return this;
    }

    public Validate notEqualTo(String text) {
        String actual = conductor.getText(by);
        if (actual.equals(text)) {
            fail(String.format("Text matches! [expected: %s] [actual: %s]", text, actual));
        }
        return this;
    }


    public Validate equalsToIgnoreCase(String text) {
        String actual = conductor.getText(by);
        if (!text.equalsIgnoreCase(actual)) {
            fail(String.format("Text, ignoring case, does not match! [expected: %s] [actual: %s]", text, actual));
        }
        return this;
    }


    public Validate notEqualsToIgnoreCase(String text) {
        String actual = conductor.getText(by);
        if (text.equalsIgnoreCase(actual)) {
            fail(String.format("Text, ignoring case, matches! [expected: %s] [actual: %s]", text, actual));
        }
        return this;
    }


    public Validate thatTextIsPresent(String text) {
        if (!conductor.getDriver().getPageSource().contains(text)) {
            fail(String.format("Page source does not contain '%s'", text));
        }
        return this;
    }


    public Validate thatTextIsNotPresent(String text) {
        if (conductor.getDriver().getPageSource().contains(text)) {
            fail(String.format("Page source does contain '%s'", text));
        }
        return this;
    }

    public Validate attributeEquals(String attribute, String text) {
        WebElement element = conductor.waitForElement(by);
        String actual = null;
        try {
            actual = element.getAttribute(attribute);
            if (actual.equals(text)) {
                return this;
            }
        } catch (NoSuchElementException e) {
            fail("No such element [" + element.toString() + "] exists.");
        } catch (Exception x) {
            fail("Cannot validate an attribute if an element doesn't have it!");
        }

        Pattern p = Pattern.compile(text);
        Matcher m = p.matcher(actual);

        if (!m.find()) {
            fail(String.format("Attribute doesn't match! [Selector: %s] [Attribute: %s] [Desired value: %s] [Actual value: %s]",
                    element.toString(),
                    attribute,
                    text,
                    actual));
        }
        return this;
    }


    public Validate validateFalse(boolean condition) {
        return validateFalse(condition, "Condition is not false");
    }

    public Validate validateFalse(boolean condition, String message) {
        return validateTrue(!condition, message);
    }

    public Validate validateTrue(boolean condition) {
        return validateTrue(condition, "Condition is not true");
    }

    public Validate validateTrue(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
        return this;
    }

}
