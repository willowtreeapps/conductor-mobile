package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.ScreenShotUtil;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Created on 2/10/17.
 */
public abstract class Watchman extends TestWatcher {

    public abstract Locomotive getLocomotive();
    public abstract void quit();

    private boolean failure;
    private Throwable e;

    @Override
    protected void failed(Throwable e, Description description) {
        if (getLocomotive().configuration.shouldScreenshotOnFail()) {
            failure = true;
            this.e = e;
        }
    }

    /**
     * Take screenshot if the test failed.
     */
    @Override
    protected void finished(Description description) {
        super.finished(description);
        if (getLocomotive().configuration.shouldScreenshotOnFail()) {
            if (failure) {
                ScreenShotUtil.take(getLocomotive(),
                        description.getDisplayName(),
                        e.getMessage());
            }
        }
        quit();
    }
}
