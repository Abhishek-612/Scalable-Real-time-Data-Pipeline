package com.pipeline.datapipeline.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.pipeline.datapipeline.beans.DataModel;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
public class DataStreamerService {

    private final Logger LOGGER = LogManager.getLogger();
    private Producer<String, Object> producer = null;

    @Value("${kafka.bootstrap.servers}")
    private String KAFKA_BOOTSTRAP_SERVERS;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DataStreamerService() {

    }

    private Producer<String, Object> createKafkaProducer() {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

        return new KafkaProducer<>(kafkaProps);
    }

    public void processData(DataModel dataModel, JsonNode data) {
        LOGGER.debug(data);

        ProducerRecord<String, Object> record = createProducerRecord(dataModel, data);
        if (producer == null) {
            producer = createKafkaProducer();
        }
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    exception.printStackTrace();
                } else {
                    LOGGER.info("Data sent successfully to Kafka. Topic: " + metadata.topic() +
                            ", Partition: " + metadata.partition() + ", Offset: " + metadata.offset());
                }
            }
        });
    }

    private ProducerRecord<String, Object> createProducerRecord(DataModel dataModel, JsonNode value) {
        String key = LocalDateTime.now().format(formatter);
        return new ProducerRecord<>(dataModel.getName(), key, value);
    }


}