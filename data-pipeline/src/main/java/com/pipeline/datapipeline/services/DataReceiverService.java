package com.pipeline.datapipeline.services;

import com.pipeline.datapipeline.utils.Constants;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataReceiverService {
    private boolean running;
    Consumer<String, String> consumer = null;

    @Value("${kafka.consumer.group-id}")
    private String GROUP_ID;
    @Value("${kafka.bootstrap.servers}")
    private String BOOTSTRAP_SERVERS;
    @Value("${kafka.consumer.auto-offset-reset}")
    private String AUTO_OFF_RESET;

    private static final Logger LOGGER = LogManager.getLogger();


    public DataReceiverService() {
        this.running = false;
    }

    public void kafkaInit() {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFF_RESET);

        consumer = new KafkaConsumer<>(kafkaProps);

        LOGGER.info("Kafka Connection Established!");
    }

    public void start() {
        // Initialize necessary resources
        // Connect to external systems, set up data sources, etc.
        BOOTSTRAP_SERVERS = Constants.serverAddress;
        kafkaInit();

        // Start receiving data
        running = true;
        while (running) {
            // Receive data from a data source
            receiveData();

            // Process the received data
//            process(data);
        }

        // Clean up resources
        // Disconnect from external systems, close connections, etc.
    }

    public void stop() {
        // Set running flag to false to stop the data receiving loop
        running = false;
        consumer.close();
        LOGGER.info("Kafka Disconnected!");
    }

    private void receiveData() {
        String TOPIC = "data-topic";

        try {
            consumer.subscribe(Collections.singleton(TOPIC));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
                for (ConsumerRecord<String, String> record : records) {
                    String data = record.value();
                    LOGGER.info("Received data: " + data);
                }
                consumer.commitSync(); // Commit the offsets to mark the messages as processed
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void process(Data data) {
//        // Implement the logic to process the received data
//        // Apply any necessary transformations, validations, or other processing operations
//        // Pass the processed data to the appropriate components for further processing or storage
//    }
}
