package com.kafkaloginfluxconnector;

import com.kafkaloginfluxconnector.influxdb.InfluxDBConfig;
import com.kafkaloginfluxconnector.kafka.consumer.KafkaConsumerConfig;
import com.kafkaloginfluxconnector.kafka.consumer.KafkaConsumerGroup;

public class Main
{
    public static void main( String[] args )
    {
        String kafkaBootstrapServers = System.getProperty("kafka.url");
        String kafkaTopic = System.getProperty("kafka.topic");

        String influxServerUrl = System.getProperty("influx.url");
        String influxDatabase = System.getProperty("influx.database");

        if (kafkaBootstrapServers == null || kafkaTopic == null || influxServerUrl == null || influxDatabase == null){
            throw new NullPointerException("All System Properties not set.");
        }

        KafkaConsumerConfig.setBootstrapServers(kafkaBootstrapServers);
        KafkaConsumerConfig.setTopic(kafkaTopic);

        InfluxDBConfig.setServerUrl(influxServerUrl);
        InfluxDBConfig.setDatabase(influxDatabase);

        KafkaConsumerGroup.start();
    }
}
