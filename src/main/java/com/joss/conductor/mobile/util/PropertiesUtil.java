package com.joss.conductor.mobile.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Created on 9/14/16.
 */
public class PropertiesUtil {

    private static final String DEFAULT_PROPERTIES_PATH = "/default.properties";

    public static Properties getDefaultProperties(Object o) {
        Properties props = new Properties();
        try {
            props.load(o.getClass().getResourceAsStream(DEFAULT_PROPERTIES_PATH));
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
