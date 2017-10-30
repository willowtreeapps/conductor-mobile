package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.ScreenShotUtil;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Created on 3/21/17.
 */
public class TestListener implements ITestListener {

    public void onTestStart(ITestResult result) {

    }

    public void onTestSuccess(ITestResult result) {

    }

    public void onTestFailure(ITestResult result) {
        if(result instanceof Locomotive) {
            Locomotive locomotive = (Locomotive) result.getInstance();
            if (locomotive.configuration.shouldScreenshotOnFail()) {
                ScreenShotUtil.take(locomotive,
                        result.getTestClass().getName() + "." + result.getMethod().getMethodName(),
                        result.getThrowable().getMessage());
            }
        }
    }

    public void onTestSkipped(ITestResult result) {

    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    public void onStart(ITestContext context) {

    }

    public void onFinish(ITestContext context) {

    }
}
