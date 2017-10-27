package com.joss.conductor.mobile.config;

import com.joss.conductor.mobile.Platform;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Repeatable(LocomotivePropertiesCollection.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocomotiveProperties {
    Platform platform() default Platform.NONE;
    String file();
}