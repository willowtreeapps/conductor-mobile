package com.joss.conductor.mobile.annotation;

import com.joss.conductor.mobile.Platform;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertEquals;

/**
 * Created on 3/14/18.
 */
public class PlatformIdHelperTest {

    @Test(expected = ClassCastException.class)
    public void testIsByType() throws Exception {
        Platform platform = Platform.ANDROID;
        PlatformIdHelper.initIds(new InvalidStubPage(), platform);
    }

    @Test
    public void testItemIdPopulatedAndroid() throws Exception {
        StubPage page = new StubPage();
        Platform platform = Platform.ANDROID;
        PlatformIdHelper.initIds(page, platform);

        assertEquals(page.Item, By.id("google"));
    }

    @Test
    public void testItemIdPopulatedIos() throws Exception {
        StubPage page = new StubPage();
        Platform platform = Platform.IOS;
        PlatformIdHelper.initIds(page, platform);

        assertEquals(page.Item, By.id("apple"));
    }

    @Test
    public void testItemIdPopulatedCorrectAndroid() throws Exception {
        StubPage page = new StubPage();
        Platform platform = Platform.ANDROID;
        PlatformIdHelper.initIds(page, platform);

        assertEquals(page.SecondItem, By.id("elgoog"));
    }

    @Test
    public void testItemIdPopulatedCorrectIos() throws Exception {
        StubPage page = new StubPage();
        Platform platform = Platform.IOS;
        PlatformIdHelper.initIds(page, platform);

        assertEquals(page.SecondItem, By.id("elppa"));
    }

    @Test
    public void testXpathAndroid() throws Exception {
        StubPage page = new StubPage();
        Platform platform = Platform.ANDROID;
        PlatformIdHelper.initIds(page, platform);

        assertEquals(page.XpathItem, By.xpath("//*[@text='Knock Knock']"));
    }

    @Test
    public void testXpathIos() throws Exception {
        StubPage page = new StubPage();
        Platform platform = Platform.IOS;
        PlatformIdHelper.initIds(StubPage.class, platform);

        assertEquals(page.XpathItem, By.xpath("//*[@text='Who's there?']"));
    }


}
