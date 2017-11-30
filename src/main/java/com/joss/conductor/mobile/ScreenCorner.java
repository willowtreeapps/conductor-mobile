package com.joss.conductor.mobile;

import org.openqa.selenium.Point;


public enum ScreenCorner {
    NONE("none"),
    TOP_LEFT("top left"),
    TOP_RIGHT("top_right"),
    BOTTOM_LEFT("bottom left"),
    BOTTOM_RIGHT("bottom right");




    String corner;
    ScreenCorner(String corner) { this.corner = corner;}


}