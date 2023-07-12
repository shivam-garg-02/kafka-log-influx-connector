package com.kafkaloginfluxconnector.kafka.consumer;

import com.kafkaloginfluxconnector.influxdb.InfluxDBService;
import com.kafkaloginfluxconnector.kafka.message.LogFieldRegexParser;
import com.kafkaloginfluxconnector.kafka.model.LogMessageMetadata;
import com.kafkaloginfluxconnector.counter.MessageCounter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;

public class ConsumerThread extends Thread{
    private final KafkaConsumer<String, String> CONSUMER;

    public ConsumerThread(KafkaConsumer<String, String> consumer) {
        this.CONSUMER = consumer;
    }

    public KafkaConsumer<String, String> getConsumer() {
        return CONSUMER;
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    ConsumerRecords<String, String> records = CONSUMER.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        String message = record.value();

                        MessageCounter.incrementMessagesRead();
                        LogMessageMetadata logMessageMetadata = LogFieldRegexParser.parseLogMessage(message);

                        if (logMessageMetadata == null){
                            continue;
                        }

                        for (LogMessageMetadata.ExceptionInfo exceptionInfo : logMessageMetadata.getException()) {
                            InfluxDBService.writeDataToInfluxDB(logMessageMetadata.getTimestamp(), logMessageMetadata.getHostname(), exceptionInfo.getExceptionType());
                        }

                        MessageCounter.incrementMessagesLogged();
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
