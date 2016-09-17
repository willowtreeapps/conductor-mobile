package com.joss.conductor.mobile;

/**
 * Created on 8/11/16.
 */
public enum Platform {
    NONE("none"),
    ANDROID("android"),
    IOS("ios");

    String platform;
    Platform(String platform) {
        this.platform = platform;
    }
}
