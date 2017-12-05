package com.joss.conductor.mobile;

import org.openqa.selenium.Point;


public enum ScreenCorner {
    TOP_LEFT("top left"),
    TOP_RIGHT("top right"),
    BOTTOM_LEFT("bottom left"),
    BOTTOM_RIGHT("bottom right");




    String corner;
    ScreenCorner(String corner) { this.corner = corner;}


}