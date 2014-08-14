package com.capgemini;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * User: olavgjerde
 * Date: 13/08/14
 * Time: 15:09
 */
public class AppLogger {

    public AppLogger(){
        PropertyConfigurator.configure(App.getProperties("log4j.properties"));
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }
}
