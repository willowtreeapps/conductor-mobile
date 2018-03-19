package com.joss.conductor.mobile.annotation;

import com.joss.conductor.mobile.Platform;
import com.joss.conductor.mobile.exception.PlatformFindByException;
import org.openqa.selenium.By;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Created on 3/14/18.
 */
public final class PlatformFindByHelper {

    public static void initIds(Object page, Platform platform) {

        try {
            Class current = page.getClass();
            do {
                for (Field field : current.getDeclaredFields()) {
                    for (Annotation annotation : field.getDeclaredAnnotations()) {

                        if (annotation instanceof AndroidFindBy || annotation instanceof IOSFindBy) {
                            if (!field.getType().equals(By.class)) {
                                throw new PlatformFindByException(field.getName() + " must be of type By!");
                            }
                        }

                        if (annotation instanceof AndroidFindBy && platform.equals(Platform.ANDROID)) {
                            AndroidFindBy androidFindByAnnotation = (AndroidFindBy) annotation;
                            if (!androidFindByAnnotation.value().isEmpty()) {
                                field.set(page, By.id(androidFindByAnnotation.value()));
                            } else if (!androidFindByAnnotation.xpath().isEmpty()) {
                                field.set(page, By.xpath(androidFindByAnnotation.xpath()));
                            }
                        }

                        if (annotation instanceof IOSFindBy && platform.equals(Platform.IOS)) {
                            IOSFindBy iOSFindByAnnotation = (IOSFindBy) annotation;
                            if (!iOSFindByAnnotation.value().isEmpty()) {
                                field.set(page, By.name(iOSFindByAnnotation.value()));
                            } else if (!iOSFindByAnnotation.xpath().isEmpty()) {
                                field.set(page, By.xpath(iOSFindByAnnotation.xpath()));
                            }
                        }
                    }
                }
                current = current.getSuperclass();
            } while (current != null);
        } catch (Exception e) {
            throw new PlatformFindByException(e.getMessage());
        }

    }
}

