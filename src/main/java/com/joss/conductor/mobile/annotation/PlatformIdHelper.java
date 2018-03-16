package com.joss.conductor.mobile.annotation;

import com.joss.conductor.mobile.Platform;
import org.openqa.selenium.By;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Created on 3/14/18.
 */
public final class PlatformIdHelper {

    public static void initIds(Object page, Platform platform) throws Exception {

        for (Field field : page.getClass().getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (!field.getType().equals(By.class)) {
                    throw new ClassCastException("Must be of type By! Field name: " + field.getName());
                }

                if (annotation instanceof AndroidId) {
                    AndroidId androidIdAnnotation = (AndroidId) annotation;
                    if (platform.equals(Platform.ANDROID)) {
                        if (!androidIdAnnotation.value().isEmpty()) {
                            field.set(page, By.id(androidIdAnnotation.value()));
                        } else if (!androidIdAnnotation.xpath().isEmpty()) {
                            field.set(page, By.xpath(androidIdAnnotation.xpath()));
                        }
                    }
                }
                if (annotation instanceof IOSId) {
                    IOSId iOSIdAnnotation = (IOSId) annotation;
                    if (platform.equals(Platform.IOS)) {
                        if (!iOSIdAnnotation.value().isEmpty()) {
                            field.set(page, By.id(iOSIdAnnotation.value()));
                        } else if (!iOSIdAnnotation.xpath().isEmpty()) {
                            field.set(page, By.xpath(iOSIdAnnotation.xpath()));

                        }
                    }
                }
            }
        }
    }
}
