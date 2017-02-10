package com.joss.conductor.mobile.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created on 9/14/16.
 */
public class PropertiesUtil {

    private static final String DEFAULT_PROPERTIES_PATH = "/default.properties";

    public static Properties getDefaultProperties(Object object) {
        Properties props = new Properties();
        try {
            InputStream inputStream = object.getClass().getResourceAsStream(DEFAULT_PROPERTIES_PATH);
            if (inputStream != null) {
                props.load(inputStream);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Log.fatal("Couldn\'t load in default properties");
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.fatal("Couldn\'t load in default properties");
        }
        return props;
    }

}
