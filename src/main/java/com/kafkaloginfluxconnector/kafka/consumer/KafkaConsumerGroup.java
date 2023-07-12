package com.kafkaloginfluxconnector.kafka.consumer;

import com.kafkaloginfluxconnector.counter.CounterThread;
import com.kafkaloginfluxconnector.influxdb.InfluxDBService;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KafkaConsumerGroup {
    private static final List<KafkaConsumer<String, String>> CONSUMERS = new ArrayList<>();
    private static final List<ConsumerThread> CONSUMER_THREADS = new ArrayList<>();
    private static final int NUM_CONSUMERS = 5;

    public static void initializeClient(){
        InfluxDBService.initializeClient();

        for (int i = 0; i < NUM_CONSUMERS; i++) {
            CONSUMERS.add(new KafkaConsumer<>(KafkaConsumerConfig.getProperties()));
        }
    }

    public static void start(){
        initializeClient();

        for (KafkaConsumer<String, String> consumer : CONSUMERS) {
            consumer.subscribe(Collections.singleton(KafkaConsumerConfig.getTopic()));
        }

        for (KafkaConsumer<String, String> consumer : CONSUMERS) {
            ConsumerThread consumerThread = new ConsumerThread(consumer);
            consumerThread.start();
            CONSUMER_THREADS.add(consumerThread);
        }

        CounterThread counterThread = new CounterThread();
        counterThread.start();

        monitor();
    }

    private static void monitor(){
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (ConsumerThread consumerThread : CONSUMER_THREADS) {
                if (!consumerThread.isAlive()) {
                    ConsumerThread newConsumerThread = new ConsumerThread(consumerThread.getConsumer());
                    newConsumerThread.start();
                    CONSUMER_THREADS.remove(consumerThread);
                    CONSUMER_THREADS.add(newConsumerThread);
                }
            }
        }
    }
}
