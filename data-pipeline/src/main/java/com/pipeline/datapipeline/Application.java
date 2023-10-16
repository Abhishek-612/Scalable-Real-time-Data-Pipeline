package com.pipeline.datapipeline;

import com.pipeline.datapipeline.beans.DataModel;
import com.pipeline.datapipeline.controllers.ApiStreamController;
import com.pipeline.datapipeline.controllers.DataReceiverController;
import com.pipeline.datapipeline.controllers.DataStreamController;
import com.pipeline.datapipeline.utils.Constants;
import com.pipeline.datapipeline.webcomponents.MenuOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
public class Application {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		LOGGER.info("DataSurge Application Started");
		ApplicationContext context = SpringApplication.run(Application.class, args);
		MenuOptions menuOptions = context.getBean(MenuOptions.class);
	}
}

