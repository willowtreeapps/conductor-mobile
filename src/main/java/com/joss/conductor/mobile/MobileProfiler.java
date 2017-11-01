package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.ArtifactUtil;
import com.joss.conductor.mobile.util.DeviceUtil;
import com.joss.conductor.mobile.util.Log;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * Instrumentation is used for launching a platform's profiling process
 * while an automated test is running.
 */
public class MobileProfiler {

    Locomotive driver;
    String testName;
    Process process;

    private MobileProfiler() {

    }

    public static MobileProfiler launch(@NotNull Locomotive driver, @NotNull String testName) {
        MobileProfiler mp = new MobileProfiler();

        mp.driver = driver;
        mp.testName = testName;
        mp.start();

        return mp;
    }

    private void start() {
        try {
            switch (driver.configuration.getPlatformName()) {
                case IOS:
                    startIosInstrumentation();
                    break;
                case ANDROID:
                    startAndroidInstrumentation();
                    break;
                case NONE:
                    throw new InvalidParameterException("You must have a platform set to perform instrumentation");
            }
        } catch(IOException e) {
            Log.log.severe("IOException attempting to start instrumentation: " + e.toString());
        }
    }

    private void startIosInstrumentation() throws IOException {
        // Verify we have everything set
        if(driver.configuration.getAppPackageName() != null) {

        }

        // Find connected UDID if none specified
        String udid = driver.configuration.getUdid();
        if(udid == null) {
            String[] devices = DeviceUtil.getConnectedDevices(Platform.IOS);
            if(devices != null && devices.length > 0) {
                udid = devices[0];
            }
        }

        if(udid == null) {
            throw new NullPointerException("MobileProfiler tests require a connected device");
        }

        String traceArtifactName = ArtifactUtil.artifactPathForTest(testName, ".trace");
        String logArtifactName = ArtifactUtil.artifactPathForTest(testName, ".log");
        ProcessBuilder pb = new ProcessBuilder("instruments",
                "-t", "Energy Log",
                "-D", traceArtifactName,
                "-w", udid,
                "-v",
                driver.configuration.getAppPackageName()
        );
        pb.redirectErrorStream(true);
        pb.redirectOutput(new File(logArtifactName));
        process = pb.start();
    }

    private void startAndroidInstrumentation() {

    }
}
