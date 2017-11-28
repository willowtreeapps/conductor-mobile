package com.joss.conductor.mobile.util;

import com.joss.conductor.mobile.Locomotive;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created on 7/25/16.
 */
public class ScreenShotUtil {

    private static final String SCREENSHOT_EXT = ".png";

    public static void take(Locomotive locomotive, String testName) {
        String artifactName = ArtifactUtil.artifactPathForTest(testName, SCREENSHOT_EXT);
        writeFile(locomotive.driver, artifactName);
    }

    public static void take(Locomotive locomotive, String path, String testName) {
        String artifactName = ArtifactUtil.artifactPathForTest(path, testName, SCREENSHOT_EXT);
        writeFile(locomotive.driver, artifactName);
    }

    private static void writeFile(AppiumDriver appiumDriver, String filePathAndName) {
        try {
            FileUtils.copyFile(appiumDriver.getScreenshotAs(OutputType.FILE), new File(filePathAndName));
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}

