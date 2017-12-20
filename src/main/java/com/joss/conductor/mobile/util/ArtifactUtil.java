package com.joss.conductor.mobile.util;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class ArtifactUtil {

    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.S");
    public static final String DIR = "target/test-artifacts";
    public static final String WORKING_DIR = System.getProperty("user.dir");

    private static final int MaxFileLength = 100;

    public static String artifactPathForTest(String testName, String extension) {
        String timeStamp = getTimestamp();
        int testLengthLimit = MaxFileLength - timeStamp.length() - 1;
        String file = sanitizePath(testName, testLengthLimit);
        return WORKING_DIR
                + File.separator
                + DIR
                + File.separator
                + file
                + "-" +  timeStamp
                + (extension == null ? "" : extension);
    }

    public static String artifactPathForTest(String basePath, String testName, String extension) {
        return WORKING_DIR
                + File.separator
                + DIR
                + File.separator
                + sanitizePath(basePath, 100)
                + File.separator
                + sanitizePath(testName, 100)
                + "-" +  getTimestamp()
                + (extension == null ? "" : extension);
    }

    private static String getTimestamp() {
        return SDF.format(new Timestamp(System.currentTimeMillis()));
    }

    private static String sanitizePath(String path, int limit) {
        if(path == null)
            return "(null)";

        path = path.replaceAll("[\\/\\\"\\*\\?\\:\\<\\>\\|\\\\]", "+");

        if(path.length() > limit) {
            path = path.substring(0, limit);
        }
        return path;
    }
}
