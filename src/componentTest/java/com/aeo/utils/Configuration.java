package com.aeo.utils;

import com.aeo.constants.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Configuration {

    Properties prop = new Properties();

    public Configuration() {
        try (InputStream input = new FileInputStream(
                String.format("%s/%s.properties", Constants.RESOURCES_PATH, System.getProperty("env") != null ? System.getProperty("env") : "sit"))) {
            prop.load(input);
        } catch (IOException ex) {
            log.error(String.format("Configuration file \"%s\" was not found", System.getProperty("env")
                    + ".properties"));
        }
    }

    public String getServiceUrl() {
        return prop.getProperty("authServiceUrl");
    }

    public String getToken() {
        return prop.getProperty("authKey");
    }

    public String getProperty(String property) {
        return prop.getProperty(property);
    }

}
