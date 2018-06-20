package com.joss.conductor.mobile.annotation;

import java.lang.annotation.*;

/**
 * Created on 3/14/18.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IOSFindBy {

    //value() gets set By.name() by PlatformFindByHelper.
    String value() default "";

    String xpath() default "";

    String className() default "";
}
