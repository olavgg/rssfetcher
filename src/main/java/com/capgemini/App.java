package com.capgemini;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: olavgjerde
 * Date: 14/08/14
 * Time: 13:28
 */
public class App {

    public static Properties config = App.getProperties("app.properties");

    public static Properties getProperties(String resourceName){
        ClassLoader loader =
                Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try(InputStream resourceStream =
                    loader.getResourceAsStream(resourceName)) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
