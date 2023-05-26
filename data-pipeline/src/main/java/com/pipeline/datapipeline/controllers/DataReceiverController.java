package com.pipeline.datapipeline.controllers;

import com.pipeline.datapipeline.services.DataReceiverService;
import com.pipeline.datapipeline.services.DataStreamerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DataReceiverController {

    private DataReceiverService dataReceiverService;

    @Autowired
    public DataReceiverController(DataStreamerService dataStreamService, DataReceiverService dataReceiverService) {
        this.dataReceiverService = dataReceiverService;
    }

    private static final Logger LOGGER = LogManager.getLogger();

    public void startStreamController() {


        Thread receiverThread = new Thread(() -> {
            dataReceiverService.start();
        });
        receiverThread.start();



        // Register shutdown hook to gracefully stop the application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Stop the data processing pipeline
            dataReceiverService.stop();

            try {
                receiverThread.join();
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
    }
}
