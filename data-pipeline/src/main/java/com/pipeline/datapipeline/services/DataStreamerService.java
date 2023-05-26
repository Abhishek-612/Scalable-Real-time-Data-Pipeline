package com.pipeline.datapipeline.services;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

@Service
public class DataStreamerService {

    private String TOPIC = "data-topic";
    private boolean running;

    @Value("${kafka.bootstrap.servers}")
    private String BOOTSTRAP_SERVERS;

    private static final Logger LOGGER = LogManager.getLogger();
    Producer<String, String> producer = null;

    public DataStreamerService() {
        this.running = false;
    }

    public void start() {
        // Initialize necessary resources
        // Connect to external systems, set up data sources, etc.
        System.out.println(BOOTSTRAP_SERVERS);
        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(kafkaProps);

        // Start receiving data
        running = true;
        while (running) {
            // Receive data from a data source
            storeData();
        }

        // Clean up resources
        // Disconnect from external systems, close connections, etc.
    }

    public void stop() {
        // Set running flag to false to stop the data receiving loop
        running = false;
    }

    public void storeData() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;


            while ((line = reader.readLine()) != null) {
                // Process the input line as needed
                // You can modify this part to parse and format the data according to your requirements

                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, line);
                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        if (exception != null) {
                            exception.printStackTrace();
                        } else {
                            System.out.println("Data sent successfully to Kafka. Topic: " + metadata.topic() +
                                    ", Partition: " + metadata.partition() + ", Offset: " + metadata.offset());
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
