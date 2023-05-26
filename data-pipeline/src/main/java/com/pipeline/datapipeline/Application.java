package com.pipeline.datapipeline;

import com.pipeline.datapipeline.controllers.DataReceiverController;
import com.pipeline.datapipeline.controllers.DataStreamController;
import com.pipeline.datapipeline.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Application.class, args);

		if(args.length>1) {
			Constants.setServerAddress(args[1]);

			switch(args[0]) {
				case "stream":
					DataStreamController dataStreamController = context.getBean(DataStreamController.class);
					dataStreamController.startStreamController();
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
			LOGGER.error("Could not locate the server");
	}

}
