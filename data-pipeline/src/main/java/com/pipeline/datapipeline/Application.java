package com.pipeline.datapipeline;

import com.pipeline.datapipeline.controllers.DataReceiverController;
import com.pipeline.datapipeline.controllers.DataStreamController;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.logging.LogManager;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Application.class, args);

		if(args.length>0) {
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
					System.out.println("Wrong Input");
			}
		}
	}

}
