package com.kafkaloginfluxconnector.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class KafkaConsumerConfig {
    private static String bootstrapServers;
    private static String groupId = "my-consumer-group";
    private static String topic;

    public static void setBootstrapServers(String bootstrapServers) {
        KafkaConsumerConfig.bootstrapServers = bootstrapServers;
    }

    public static void setGroupId(String groupId) {
        KafkaConsumerConfig.groupId = groupId;
    }

    public static void setTopic(String topic) {
        KafkaConsumerConfig.topic = topic;
    }

    @NotNull
    public static Properties getProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return properties;
    }

    public static String getTopic() {
        return topic;
    }
}

