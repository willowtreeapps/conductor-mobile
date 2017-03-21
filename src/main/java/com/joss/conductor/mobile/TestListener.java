package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.ScreenShotUtil;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Created on 3/21/17.
 */
public class TestListener implements ITestListener {

    private Locomotive locomotive;

    public void onTestStart(ITestResult result) {
        this.locomotive = (Locomotive) result.getInstance();
    }

    public void onTestSuccess(ITestResult result) {

    }

    public void onTestFailure(ITestResult result) {
        if (locomotive.configuration.screenshotsOnFail()) {
                ScreenShotUtil.take(locomotive,
                        result.getTestName(),
                        result.getThrowable().getMessage());
        }
    }

    public void onTestSkipped(ITestResult result) {

    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    public void onStart(ITestContext context) {

    }

    public void onFinish(ITestContext context) {
        locomotive.driver.quit();
    }
}
