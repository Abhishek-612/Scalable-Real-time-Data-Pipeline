package com.pipeline.datapipeline.controllers;

import com.pipeline.datapipeline.services.DataReceiver;
import com.pipeline.datapipeline.services.DataStreamer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StreamController {

    private DataStreamer dataStream;
    private DataReceiver dataReceiver;

    @Autowired
    public StreamController(DataStreamer dataStream, DataReceiver dataReceiver) {
        this.dataStream = dataStream;
        this.dataReceiver = dataReceiver;
    }

    public void startStreamController() {


        Thread receiverThread = new Thread(() -> {
            dataReceiver.start();
        });
        receiverThread.start();

        // Start the data processing pipeline
        Thread streamThread = new Thread(() -> {
            dataStream.start();
        });
        streamThread.start();



        // Register shutdown hook to gracefully stop the application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Stop the data processing pipeline
            dataStream.stop();
            dataReceiver.stop();

            try {
                streamThread.join();
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
