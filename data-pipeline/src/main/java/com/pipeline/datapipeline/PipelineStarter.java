package com.pipeline.datapipeline;

import com.pipeline.datapipeline.beans.DataModel;
import com.pipeline.datapipeline.controllers.ApiStreamController;
import com.pipeline.datapipeline.controllers.DataReceiverController;
import com.pipeline.datapipeline.dao.DatabaseService;
import com.pipeline.datapipeline.dao.databases.DatabaseSource;
import com.pipeline.datapipeline.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
public class PipelineStarter implements DisposableBean {

    private static final Logger LOGGER = LogManager.getLogger();
    private final ApiStreamController apiStreamController;
    private final DataModelLoader dataModelLoader;
    private final DataReceiverController dataReceiverController;
    private final ThreadPoolExecutor executorService;


    @Autowired
    public PipelineStarter(ApiStreamController apiStreamController, DataModelLoader dataModelLoader, DataReceiverController dataReceiverController) {
        this.apiStreamController = apiStreamController;
        this.dataModelLoader = dataModelLoader;
        this.dataReceiverController = dataReceiverController;

        this.dataModelLoader.loadAndParseDataModels();

        // Create a thread pool with an adjustable thread pool size
        int corePoolSize = Runtime.getRuntime().availableProcessors(); // Use the number of available CPU cores as the initial pool size
        int maximumPoolSize = corePoolSize * 2; // Set the maximum pool size based on desired concurrency level
        long keepAliveTime = Constants.THREAD_ALIVE_TIME; // Keep idle threads alive for 60 seconds
        this.executorService = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
    }

    @PostMapping("/stream")
    public void startDataStreamer(@RequestParam String apiName) {
        LOGGER.info("Data Streamer Started for: " + apiName);

        // Load and parse data models
        DataModel dataModel = dataModelLoader.getDataModelByName(apiName);
        if(dataModel == null)
            LOGGER.error("No data model found linked to: " + apiName);
        else
            // Submit each data model for streaming concurrently
            this.executorService.submit(() -> apiStreamController.startStreaming(dataModel));
    }

    @PostMapping("/fetch")
    public void startDataReceiver(@RequestParam String apiName) {
        LOGGER.info("Data Receiver Started for: "+apiName);
        dataReceiverController.startReceiverController();
    }

    @Override
    public void destroy() throws Exception {
        // Shutdown the executor service when all tasks are completed
        this.executorService.shutdown();
    }
}
