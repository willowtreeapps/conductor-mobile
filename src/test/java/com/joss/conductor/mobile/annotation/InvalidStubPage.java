package com.joss.conductor.mobile.annotation;

/**
 * Created on 3/14/18.
 */
public class InvalidStubPage {

    @AndroidFindBy("google2")
    @IOSFindBy("apple2")
    public String InvalidItem;
}
