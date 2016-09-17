package com.joss.conductor.mobile;

public enum SwipeElementDirection {
    NONE("none"),
    UP("up"),
    RIGHT("right"),
    DOWN("down"),
    LEFT("left");

    String direction;
    SwipeElementDirection(String direction) {
        this.direction = direction;
    }
}
