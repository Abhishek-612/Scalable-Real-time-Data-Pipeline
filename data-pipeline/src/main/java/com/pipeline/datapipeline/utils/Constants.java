package com.pipeline.datapipeline.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {

    private static final Logger LOGGER = LogManager.getLogger();
    public static String serverAddress = "localhost:9092";

    public static void setServerAddress(String serverAddress) {
        Constants.serverAddress = serverAddress;
        LOGGER.info("Current Server Address: "+Constants.serverAddress);
    }

}
