package com.pipeline.datapipeline;

import com.pipeline.datapipeline.beans.DataModel;
import com.pipeline.datapipeline.controllers.ApiStreamController;
import com.pipeline.datapipeline.controllers.DataReceiverController;
import com.pipeline.datapipeline.controllers.DataStreamController;
import com.pipeline.datapipeline.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Application.class, args);

		if (args.length > 0) {
			switch (args[0]) {
				case "stream":
					ApiStreamController apiStreamController = context.getBean(ApiStreamController.class);
					DataModelLoader dataModelLoader = context.getBean(DataModelLoader.class);

					// Load and parse data models
					List<DataModel> dataModels = dataModelLoader.loadAndParseDataModels();

					// Create a thread pool with an adjustable thread pool size
					int corePoolSize = Runtime.getRuntime().availableProcessors(); // Use the number of available CPU cores as the initial pool size
					int maximumPoolSize = corePoolSize * 2; // Set the maximum pool size based on desired concurrency level
					long keepAliveTime = 60; // Keep idle threads alive for 60 seconds
					ThreadPoolExecutor executorService = new ThreadPoolExecutor(
							corePoolSize,
							maximumPoolSize,
							keepAliveTime,
							TimeUnit.SECONDS,
							new LinkedBlockingQueue<>());

					// Submit each data model for streaming concurrently
					for (DataModel dataModel : dataModels) {
						executorService.submit(() -> apiStreamController.startStreaming(dataModel));
					}

					// Shutdown the executor service when all tasks are completed
					executorService.shutdown();
					break;
				case "fetch":
					DataReceiverController dataReceiverController = context.getBean(DataReceiverController.class);
					dataReceiverController.startStreamController();
					break;
				default:
					LOGGER.error("Wrong Input");
			}
		} else {
			LOGGER.error("No Input");
		}
	}
}

