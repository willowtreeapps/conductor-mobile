package com.joss.conductor.mobile.annotation;

import java.lang.annotation.*;

/**
 * Created on 3/14/18.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IOSId {

    String value() default "";

    String xpath() default "";

}