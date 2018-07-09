package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.ScreenShotUtil;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Created on 2/10/17.
 */
public abstract class Watchman extends TestWatcher {

    public abstract Locomotive getLocomotive();
    public abstract void quit();

    private boolean failure;
    private boolean skipped;
    private Throwable e;

    @Override
    protected void failed(Throwable e, Description description) {
        if (getLocomotive().configuration.isScreenshotOnFail()) {
            failure = true;
            this.e = e;
        }
    }
    @Override
    protected void skipped(AssumptionViolatedException e, Description description) {
        if(getLocomotive().configuration.isScreenshotOnSkip()) {
            skipped = true;
            this.e = e;
        }
    }

    /**
     * Take screenshot if the test failed.
     */
    @Override
    protected void finished(Description description) {
        super.finished(description);
        if (getLocomotive().configuration.isScreenshotOnFail()) {
            if (failure) {
                ScreenShotUtil.take(getLocomotive(),
                        description.getDisplayName(),
                        e.getMessage());
            }
        }
        if (getLocomotive().configuration.isScreenshotOnSkip()) {
            if (skipped) {
                ScreenShotUtil.take(getLocomotive(),
                        description.getDisplayName(),
                        e.getMessage());
            }
        }
        quit();
    }
}
