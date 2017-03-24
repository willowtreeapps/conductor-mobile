package com.joss.conductor.mobile.util;

import com.joss.conductor.mobile.Locomotive;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.logging.LogEntry;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by ashby.bowles on 3/24/17.
 */
public class ADBLogUtil {
private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.S");
private static final String DIR = "target/test-logs";
private static final String WORKING_DIR = System.getProperty("user.dir");
private static final String LOG_EXT = ".txt";

        public static void take(Locomotive locomotive, String testName) {
            writeFile(locomotive.driver, createFilePathAndName(testName));
        }

        public static void take(Locomotive locomotive, String path, String testName) {
            writeFile(locomotive.driver, createFilePathAndName(path, testName));
        }

        private static void writeFile(AppiumDriver appiumDriver, String filePathAndName) {
            try {
                List<LogEntry> logEntries = appiumDriver.manage().logs().get("logcat").filter(Level.ALL);
                PrintWriter log_file_writer = new PrintWriter(filePathAndName);
                log_file_writer.println(logEntries);
                log_file_writer.flush();
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
                    + LOG_EXT;
        }

        public static String createFilePathAndName(String path, String testName) {
            return WORKING_DIR
                    + File.separator
                    + DIR
                    + File.separator
                    + limitLengthTo100Chars(removeInvalidFilenameChars(path))
                    + File.separator
                    + limitLengthTo100Chars(removeInvalidFilenameChars(testName)) + "-" +  getTimestamp()
                    + LOG_EXT;
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

