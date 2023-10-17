package com.pipeline.datapipeline.services;

import com.pipeline.datapipeline.dao.DatabaseService;
import com.pipeline.datapipeline.dao.databases.DatabaseSource;
import com.pipeline.datapipeline.utils.Constants;
import com.pipeline.datapipeline.utils.DBQueryResolver;
import com.pipeline.datapipeline.utils.DataBeanGenerator;
import com.pipeline.datapipeline.utils.StringManipulation;
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
    private Consumer<String, Object> consumer = null;
    private DatabaseSource db = null;

    @Value("${kafka.consumer.group-id}")
    private String KAFKA_CONSUMER_GROUP_ID;
    @Value("${kafka.bootstrap.servers}")
    private String KAFKA_BOOTSTRAP_SERVERS;
    @Value("${kafka.consumer.auto-offset-reset}")
    private String KAFKA_CONSUMER_AUTO_OFF_RESET;

    private static final Logger LOGGER = LogManager.getLogger();


    public DataReceiverService() {
        this.running = false;
    }

    private void kafkaInit() {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_CONSUMER_GROUP_ID);
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KAFKA_CONSUMER_AUTO_OFF_RESET);

        consumer = new KafkaConsumer<>(kafkaProps);

        LOGGER.info("Kafka Connection Established!");
    }

    private void databaseInit() {
        if (db == null)
            db = DatabaseService.getDatabase(Constants.MONGODB);

        if (!db.checkConnection()) {
            try {
                db.openConnection();
            } catch (Exception e) {
                LOGGER.error("Database could not be connected!");
            }
        } else {
            LOGGER.info("Database already connected!");
        }
    }

    public void start() {
        // Initialize necessary resources
        // Connect to external systems, set up data sources, etc.
        kafkaInit();
        databaseInit();

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
        String TOPIC = "Polygon-IO";
        String resolvedQuery = null;

        try {
            consumer.subscribe(Collections.singleton(TOPIC));
            while (true) {
                long startTime = System.currentTimeMillis();

                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(10));
                LOGGER.info("Records fetched: " + records.count());

                for (ConsumerRecord<String, Object> record : records) {
                    Object data = record.value();
                    LOGGER.info("Received data: " + data.toString());

                    while (!db.checkConnection()) {
                        if (System.currentTimeMillis() - startTime > Constants.DATABASE_TIMEOUT) {
                            LOGGER.error("Database connection timeout");
                            break;
                        }
                        databaseInit();
                    }

                    Object dataEntry = process(data, TOPIC);
                    if (dataEntry != null) {
                        LOGGER.info(dataEntry.toString());
                        resolvedQuery = DBQueryResolver.getQuery(Constants.MONGODB, Constants.INSERT, data);
                    }
                    LOGGER.info("Resolved Query: " + resolvedQuery);
//                    db.executeQuery(resolvedQuery);
                }
                consumer.commitSync(); // Commit the offsets to mark the messages as processed
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object process(Object data, String dataTopic) {
        /* - Process the received data
           - Apply any necessary transformations, validations, or other processing operations
           - Pass the processed data to the appropriate components for further processing or storage */

        Object dataEntry = null;
        dataEntry = DataBeanGenerator.buildDataEntryObject(StringManipulation.toCamelCase(dataTopic));
        // TODO: Populate `dataEntry` with Setters

        LOGGER.info(dataEntry.toString());
        return dataEntry;
    }
}
