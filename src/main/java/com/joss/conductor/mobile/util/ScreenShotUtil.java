package com.joss.conductor.mobile.util;

import com.joss.conductor.mobile.Locomotive;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created on 7/25/16.
 */
public class ScreenShotUtil {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.S");
    private static final String DIR = "target/test-screenshots";
    private static final String WORKING_DIR = System.getProperty("user.dir");
    private static final String PNG_EXT = ".png";

    public static void take(Locomotive locomotive, String testName) {
        writeFile(locomotive.driver, createFilePathAndName(testName));
    }

    public static void take(Locomotive locomotive, String path, String testName) {
        writeFile(locomotive.driver, createFilePathAndName(path, testName));
    }

    private static void writeFile(AppiumDriver appiumDriver, String filePathAndName) {
        try {
            FileUtils.copyFile(appiumDriver.getScreenshotAs(OutputType.FILE), new File(filePathAndName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String createFilePathAndName(String testName) {
        return WORKING_DIR
                + File.separator
                + DIR
                + File.separator
                + limitLengthTo100Chars(removeInvalidFilenameChars(testName)) + "-" +  getTimestamp()
                + PNG_EXT;
    }

    public static String createFilePathAndName(String path, String testName) {
        return WORKING_DIR
                + File.separator
                + DIR
                + File.separator
                + limitLengthTo100Chars(removeInvalidFilenameChars(path))
                + File.separator
                + limitLengthTo100Chars(removeInvalidFilenameChars(testName)) + "-" +  getTimestamp()
                + PNG_EXT;
    }

    private static String getTimestamp() {
        return SDF.format(new Timestamp(System.currentTimeMillis()));
    }

    private static String removeInvalidFilenameChars(String name) {
        return name.replace(File.separator, "-")
                .replace(":", "-")
                .replace("?", "-")
                .replace("*", "-")
                .replace("|", "-");
    }

    private static String limitLengthTo100Chars(String name) {
        return name.substring(0, name.length() > 100 ? 100 : name.length());
    }
}

