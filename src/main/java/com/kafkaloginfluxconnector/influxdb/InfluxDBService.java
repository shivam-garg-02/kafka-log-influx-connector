package com.kafkaloginfluxconnector.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

public class InfluxDBService {
    private static InfluxDB influxApi;
    private static final String MEASUREMENT_NAME = "EXCEPTION_LOG";

    public static void initializeClient() {
        influxApi = InfluxDBConfig.getInfluxApi();
        influxApi.createDatabase(InfluxDBConfig.getDatabase());
        influxApi.setDatabase(InfluxDBConfig.getDatabase());
    }

    public static void writeDataToInfluxDB(Long timestamp, String hostname, String exceptionType) {
        Point point = Point.measurement(MEASUREMENT_NAME)
                .tag("ex_type", exceptionType)
                .tag("hostname", hostname)
                .addField("count", 1)
                .time(timestamp, TimeUnit.MILLISECONDS)
                .build();

        influxApi.write(point);
    }
}


