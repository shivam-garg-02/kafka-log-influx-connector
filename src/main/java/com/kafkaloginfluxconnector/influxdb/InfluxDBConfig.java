package com.kafkaloginfluxconnector.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.jetbrains.annotations.NotNull;

public class InfluxDBConfig {
    private static String serverUrl;
    private static String database;

    public static void setServerUrl(String serverUrl) {
        InfluxDBConfig.serverUrl = serverUrl;
    }

    public static void setDatabase(String database) {
        InfluxDBConfig.database = database;
    }

    @NotNull
    public static InfluxDB getInfluxApi(){
        return InfluxDBFactory.connect(serverUrl);
    }

    public static String getDatabase(){
        return database;
    }
}
