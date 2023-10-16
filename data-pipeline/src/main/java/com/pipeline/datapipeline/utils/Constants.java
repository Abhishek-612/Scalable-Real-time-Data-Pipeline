package com.pipeline.datapipeline.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {

    private static final Logger LOGGER = LogManager.getLogger();


    public static final int THREAD_ALIVE_TIME = 60;

    // API Streaming Default Constants
    public static final int TIMEOUT_SECONDS = 10;
    public static final int BUFFER_LIMIT = 10 * 1024 * 1024;  // Set buffer limit to 10 MB
    public static final int DEFAULT_API_STREAM_RESTART_DELAY = 30;
    public static final int DEFAULT_API_STREAM_FETCH_INTERVAL = 30;


    // Data Object Configurations
    public static final String JSON = "JSON";


    // Databases
    public static final int DATABASE_TIMEOUT = 5000; // 5 seconds
    public static final String MONGODB = "MONGODB";
    public static final String CASSANDRA = "CASSANDRA";
    public static final String HBASE = "HBASE";

    // DB Query Types
    public static final String CREATE = "CREATE";
    public static final String READ = "READ";
    public static final String INSERT = "INSERT";
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String DROP = "DROP";

}
