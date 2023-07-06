package com.pipeline.datapipeline.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {

    private static final Logger LOGGER = LogManager.getLogger();


    // API Streaming Default Constants
    public static final int TIMEOUT_SECONDS = 10;
    public static final int BUFFER_LIMIT = 10 * 1024 * 1024;  // Set buffer limit to 10 MB
    public static final int DEFAULT_API_STREAM_RESTART_DELAY = 30;
    public static final int DEFAULT_API_STREAM_FETCH_INTERVAL = 30;

}
