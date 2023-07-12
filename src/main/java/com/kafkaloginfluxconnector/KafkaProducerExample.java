package com.kafkaloginfluxconnector;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class KafkaProducerExample {
    private static final String TOPIC_NAME = "my-topic-2";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String FILE_PATH = "logfile.txt";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<>(props);

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String message;
            while (true) {
                while ((message = reader.readLine()) != null) {
                    ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, message);

                    producer.send(record, (metadata, exception) -> {
                        if (exception != null) {
                            System.err.println("Error sending message: " + exception.getMessage());
                        } else {
                            System.out.println("Sent message to topic: " + metadata.topic() +
                                    ", partition: " + metadata.partition() +
                                    ", offset: " + metadata.offset());
                        }
                    });

                    producer.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        producer.close();
    }
}


