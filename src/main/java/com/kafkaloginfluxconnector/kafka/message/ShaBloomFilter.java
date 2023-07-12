package com.kafkaloginfluxconnector.kafka.message;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.kafkaloginfluxconnector.kafka.model.LogMessageMetadata;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaBloomFilter {
    private static final BloomFilter<String> BLOOM_FILTER = BloomFilter.create(Funnels.unencodedCharsFunnel(), 10000000, 0.01);
    private static final Logger LOGGER = LoggerFactory.getLogger(ShaBloomFilter.class);

    private static boolean checkAndInsertBloom(String shaId){
        if (!BLOOM_FILTER.mightContain(shaId)){
            BLOOM_FILTER.put(shaId);
            return true;
        }
        return false;
    }

    public static void insertInFilter(@NotNull LogMessageMetadata logMessageMetadata, String stackTrace){
        for(LogMessageMetadata.ExceptionInfo exceptionInfo: logMessageMetadata.getException()){
            if (checkAndInsertBloom(exceptionInfo.getUniqueUID())) {
                String log = String.format("New Exception/Stack Trace Logged\nHostname: %s\nException Type: %s\nCalling Class: %s\nCalling Method: %s\nUID: %s\n\n%s", logMessageMetadata.getHostname(), exceptionInfo.getExceptionType(), exceptionInfo.getCallingClass(), exceptionInfo.getCallingMethod(), exceptionInfo.getUniqueUID(), stackTrace);
                LOGGER.info(log);
            }
        }
    }
}
