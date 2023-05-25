package com.pipeline.datapipeline;

import com.pipeline.datapipeline.controllers.StreamController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Application.class, args);

		StreamController t = context.getBean(StreamController.class);
		t.startStreamController();
	}

}
