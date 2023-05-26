package com.pipeline.datapipeline.controllers;

import com.pipeline.datapipeline.services.DataReceiverService;
import com.pipeline.datapipeline.services.DataStreamerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataStreamController {
    private DataStreamerService dataStreamService;

    @Autowired
    public DataStreamController(DataStreamerService dataStreamService) {
        this.dataStreamService = dataStreamService;
    }

    private static final Logger LOGGER = LogManager.getLogger();

    public void startStreamController() {

        // Start the data processing pipeline
        Thread streamThread = new Thread(() -> {
            dataStreamService.start();
        });
        streamThread.start();



        // Register shutdown hook to gracefully stop the application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Stop the data processing pipeline
            dataStreamService.stop();

            try {
                streamThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        // Wait for the application to terminate
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("Data Streamer Terminated");
    }
}
