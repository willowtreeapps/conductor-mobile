package com.joss.conductor.mobile;

import com.saucelabs.testng.SauceOnDemandTestListener;
import org.testng.ITestResult;

/**
 * Adapter for 3rd party listener.
 */
public class SauceLabsListener extends SauceOnDemandTestListener {

    private static final String SELENIUM_IS_LOCAL = "SELENIUM_IS_LOCAL";
    private final ConductorConfig config;

    /**
     * Determine whether the tests are run locally before any other methods are run.
     *
     * {@link SauceOnDemandTestListener} assumes by default a remote test is run if {@link #SELENIUM_IS_LOCAL} does not
     * have a value. For conductor we assume a test run is remote if and only if a hub property is set.
     */
    public SauceLabsListener() {
        config = new ConductorConfig();

        if (config.isLocal()) {
            System.setProperty(SELENIUM_IS_LOCAL, "true");
        } else {
            if (config.isHubLocal()) {
                System.setProperty(SELENIUM_IS_LOCAL, "true");
            }
            else{
                System.setProperty(SELENIUM_IS_LOCAL, "");
            }
        }
    }

    /**
     * The default implementation always contacts the sauce labs rest service. For conductor we want to fix this to
     * happen only for remote tests.
     *
     * @param testResult Test result for the test that just passed.
     */
    @Override
    public void onTestSuccess(ITestResult testResult) {
        if (!config.isLocal()) {
            super.onTestSuccess(testResult);
        }
    }
}
