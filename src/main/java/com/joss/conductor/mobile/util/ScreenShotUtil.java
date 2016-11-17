package com.joss.conductor.mobile.util;

import com.joss.conductor.mobile.Locomotive;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.io.IOException;

/**
 * Created on 7/25/16.
 */
public class ScreenShotUtil {

    private static final String DIR = "/target/test-screenshots";
    private static final String WORKING_DIR = System.getProperty("user.dir");

    public static void take(Locomotive locomotive, String testName) {
        try {
            FileUtils.copyFile(locomotive.driver.getScreenshotAs(OutputType.FILE),
                    new File(WORKING_DIR
                            + File.separator
                            + DIR
                            + File.separator
                            + testName.replace(File.separator, "-") + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void take(Locomotive locomotive, String path, String testName) {
        try {
            FileUtils.copyFile(locomotive.driver.getScreenshotAs(OutputType.FILE),
                    new File(WORKING_DIR
                            + File.separator
                            + DIR
                            + File.separator
                            + path.replace(File.separator, "-")
                            + File.separator
                            + testName.replace(File.separator, "-") + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

