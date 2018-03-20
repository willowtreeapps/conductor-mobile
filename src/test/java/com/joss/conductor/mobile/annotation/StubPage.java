package com.joss.conductor.mobile.annotation;

import org.openqa.selenium.By;

/**
 * Created on 3/14/18.
 */
public class StubPage {

    @AndroidFindBy("google")
    @IOSFindBy("apple")
    public By Item;

    @AndroidFindBy("elgoog")
    @IOSFindBy("elppa")
    public By SecondItem;

    @AndroidFindBy(xpath = "//*[@text='Knock Knock']")
    @IOSFindBy(xpath = "//*[@text='Who's there?']")
    public By XpathItem;
}

