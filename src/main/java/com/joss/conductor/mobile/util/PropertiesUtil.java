package com.joss.conductor.mobile.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created on 9/14/16.
 */
public class PropertiesUtil {

    private static final String DEFAULT_PROPERTIES_PATH = "/default.properties";

    public static Properties getProperties(Object object, String file) {
        Properties props = new Properties();
        try {
            InputStream inputStream = object.getClass().getResourceAsStream(file);
            if (inputStream != null) {
                props.load(inputStream);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Log.fatal("Couldn\'t load properties file: " + file);
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.fatal("Couldn\'t load properties file: " + file);
        }
        return props;
    }

    public static Properties getDefaultProperties(Object object) {
        return getProperties(object, DEFAULT_PROPERTIES_PATH);
    }

}
