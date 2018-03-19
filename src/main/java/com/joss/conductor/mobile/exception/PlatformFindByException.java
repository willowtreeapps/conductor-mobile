package com.joss.conductor.mobile.exception;

/**
 * Thrown if an error occurred when trying to initialize platform ids
 */
public class PlatformFindByException extends RuntimeException {

    public PlatformFindByException(String message) {
        super(message);
    }
}
