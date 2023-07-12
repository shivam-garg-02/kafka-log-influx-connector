package com.kafkaloginfluxconnector.kafka.message;

import com.kafkaloginfluxconnector.kafka.model.LogMessageMetadata;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackTraceProcessor {
    private static final Pattern EXCEPTION_PATTERN = Pattern.compile("\\b\\w*Exception\\w*\\b");

    @NotNull
    private static String extractExceptionWord(@NotNull String stackTrace) {
        String[] lines = stackTrace.split("\\r?\\n");
        List<String> processedLines = new ArrayList<>();

        for (String line : lines) {
            if (!line.startsWith("\t")) {
                Matcher matcher = EXCEPTION_PATTERN.matcher(line);
                if (matcher.find()) {
                    processedLines.add(matcher.group());
                }
            } else {
                processedLines.add(line);
            }
        }

        String processedStackTrace = String.join("\n", processedLines);
        return processedStackTrace.trim();
    }

    @NotNull
    private static String extractClassMethod(String stackTrace) {
        stackTrace = stackTrace.replaceAll("\\([^()]*\\)", "");
        stackTrace = stackTrace.replaceAll("\\bat ", "");
        stackTrace = stackTrace.replaceAll("\\b[a-z][\\w/]*\\.", "");
        stackTrace = stackTrace.replaceAll("\\.{3}.+?(?=\\n)", "");
        stackTrace = stackTrace.replaceAll("\\n\\s*\\n", "\n");

        return stackTrace.trim();
    }

    @NotNull
    public static List<String> preprocessStackTrace(@NotNull String stackTrace) {
        List<String> splitStackTraces = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        int lineCount = 0;

        stackTrace = extractExceptionWord(extractClassMethod(stackTrace));
        String[] lines = stackTrace.split("\n");

        for (String line : lines) {
            if (!line.startsWith("\t")) {
                if (stringBuilder.length() > 0) {
                    splitStackTraces.add(stringBuilder.toString().trim());
                    stringBuilder = new StringBuilder();
                    lineCount = 0;
                }
            }
            if (lineCount < 10) {
                stringBuilder.append(line).append("\n");
                lineCount++;
            }
        }

        if (stringBuilder.length() > 0) {
            splitStackTraces.add(stringBuilder.toString().trim());
        }

        return splitStackTraces;
    }

    @NotNull
    @Contract(pure = true)
    public static List<LogMessageMetadata.ExceptionInfo> generateExceptionInfo(@NotNull List<String> splitStackTraces){
        List<LogMessageMetadata.ExceptionInfo> exceptionInfos = new ArrayList<>();

        for (String stackTrace : splitStackTraces) {
            LogMessageMetadata.ExceptionInfo exceptionInfo = new LogMessageMetadata.ExceptionInfo();

            String[] lines = stackTrace.split("\n");

            if (EXCEPTION_PATTERN.matcher(lines[0]).matches()) {
                exceptionInfo.setExceptionType(lines[0]);
            }
            else{
                continue;
            }

            if (1 < lines.length) {
                String[] parts = lines[1].replaceAll("\t", "").split("\\.");
                exceptionInfo.setCallingClass(parts[0]);
                exceptionInfo.setCallingMethod(parts[1]);
            }
            else {
                exceptionInfo.setCallingClass("");
                exceptionInfo.setCallingMethod("");
            }

            String shaID = generateUniqueId(stackTrace);
            exceptionInfo.setUniqueUID(shaID);

            exceptionInfos.add(exceptionInfo);
        }

        return exceptionInfos;
    }

    @Nullable
    private static String generateUniqueId(@NotNull String stackTrace) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(stackTrace.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

