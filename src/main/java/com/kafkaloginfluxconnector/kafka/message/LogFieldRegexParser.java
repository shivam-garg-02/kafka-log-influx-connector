package com.kafkaloginfluxconnector.kafka.message;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.kafkaloginfluxconnector.counter.MessageCounter;
import com.kafkaloginfluxconnector.kafka.model.LogMessageMetadata;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kafkaloginfluxconnector.utils.StandardFields.*;

public class LogFieldRegexParser {

    private static final LoadingCache<String, Pattern> REGEX_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<String, Pattern>() {
        @Override
        public Pattern load(@SuppressWarnings("NullableProblems") String regexString) {
            return Pattern.compile(regexString);
        }
    });

    private static final Pattern DEFAULT_REGEX = REGEX_CACHE.getUnchecked("(\\.[A-Z][A-Za-z]*Exception:)");

    private static Pattern registered_regex = DEFAULT_REGEX;

    public static synchronized void registerRegex(String regexString) {
        if (StringUtils.isBlank(regexString)) {
            throw new RuntimeException("RegexString is null");
        }
        registered_regex = REGEX_CACHE.getUnchecked(regexString);
    }

    @Nullable
    public static LogMessageMetadata parseLogMessage(String logMessage) {
        LogMessageMetadata logMessageMetadata = new LogMessageMetadata();

        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(logMessage);

            if (!jsonObject.containsKey(TIMESTAMP) || !jsonObject.containsKey(EXCEPTION)){
                return null;
            }

            // timestamp
            logMessageMetadata.setTimestamp(parseTimestamp(jsonObject.get(TIMESTAMP).toString()));

            // regex matches from stack trace
            final List<String> regexMatches = new ArrayList<>();
            String stackTrace = jsonObject.get(EXCEPTION).toString();

            Matcher matcher = registered_regex.matcher(stackTrace);

            if (matcher.find()){
                do{
                    regexMatches.add(matcher.group());
                } while (matcher.find(matcher.start()+1));
            }
            else{
                regexMatches.add(StringUtils.EMPTY);
            }

            logMessageMetadata.setRegexMatches(regexMatches);

            // exception info list
            List<LogMessageMetadata.ExceptionInfo> exceptionInfos = StackTraceProcessor.generateExceptionInfo(StackTraceProcessor.preprocessStackTrace(stackTrace));
            if (exceptionInfos.isEmpty()){
                return null;
            }
            else {
                logMessageMetadata.setException(exceptionInfos);
            }

            // hostname
            String hostname = "";
            if (jsonObject.containsKey(HOSTNAME.get(0))){
                jsonObject = (JSONObject) jsonObject.get(HOSTNAME.get(0));
                if (jsonObject.containsKey(HOSTNAME.get(1))){
                    jsonObject = (JSONObject) jsonObject.get(HOSTNAME.get(1));
                    if (jsonObject.containsKey(HOSTNAME.get(2))){
                        hostname = jsonObject.get(HOSTNAME.get(2)).toString();
                    }
                }
            }
            logMessageMetadata.setHostname(hostname);

            // insert in bloom filter
            ShaBloomFilter.insertInFilter(logMessageMetadata, stackTrace);

            // if everything correct increment counter
            MessageCounter.incrementMessagesCorrect();

        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid payload");
        }

        return logMessageMetadata;
    }

    @NotNull
    private static Long parseTimestamp(String timestamp){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        Instant instant = Instant.from(formatter.parse(timestamp));
        return instant.toEpochMilli();
    }

    public static void printLogMessageAsJSON(String logMessage, String filePath) {
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(logMessage);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(jsonObject);

            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(jsonOutput);
            fileWriter.close();

        } catch (ParseException | IOException e) {
            throw new IllegalArgumentException("Invalid Payload or Filename");
        }
    }
}
