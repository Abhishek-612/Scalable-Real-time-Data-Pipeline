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

import java.util.List;

@SpringBootApplication
public class Application {

	private static final Logger LOGGER = LogManager.getLogger();

	@Autowired
	private DataModelLoader dataModelLoader;

	public static void main(String[] args) throws InterruptedException {
		Thread.sleep(5000);
		ApplicationContext context = SpringApplication.run(Application.class, args);

		if(args.length>0) {
			switch(args[0]) {
				case "stream":
					ApiStreamController apiStreamController = context.getBean(ApiStreamController.class);
					DataModelLoader dataModelLoader = context.getBean(DataModelLoader.class);

					// Load and parse data models
					List<DataModel> dataModels = dataModelLoader.loadAndParseDataModels();

					// Create a thread for each data model and stream API data
					for (DataModel dataModel : dataModels) {
						Thread thread = new Thread(() -> apiStreamController.streamApiData(dataModel));
						thread.start();
					}
					break;
				case "fetch":
					DataReceiverController dataReceiverController = context.getBean(DataReceiverController.class);
					dataReceiverController.startStreamController();
					break;
				default:
					LOGGER.error("Wrong Input");
			}
		}
		else
			LOGGER.error("No Input");
	}

}
