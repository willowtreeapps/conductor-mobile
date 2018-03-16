package com.joss.conductor.mobile.annotation;

import org.openqa.selenium.By;

/**
 * Created on 3/14/18.
 */
public class StubPage {

    @AndroidId("google")
    @IOSId("apple")
    public By Item;

    @AndroidId("elgoog")
    @IOSId("elppa")
    public By SecondItem;

    @AndroidId(xpath = "//*[@text='Knock Knock']")
    @IOSId(xpath = "//*[@text='Who's there?']")
    public By XpathItem;
}

