package com.joss.conductor.mobile;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ConductorTest {

    private AppiumDriver driver;

    @BeforeClass
    public void init() {
        DesiredCapabilities capabilities = ConductorCapabilities.build();
        this.driver = new AndroidDriver(capabilities);
    }

    @Test
    public void normalTest() {

    }

    @AfterClass
    public void tearDown() {

    }
}
