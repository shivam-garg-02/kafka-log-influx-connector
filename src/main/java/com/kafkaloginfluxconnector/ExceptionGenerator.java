package com.kafkaloginfluxconnector;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

public class ExceptionGenerator {

    private static final String[] HOSTS = {"HostA", "HostB", "HostC", "HostD"};

    public static void main(String[] args) {
        Thread exceptionThread = new Thread(new ExceptionGeneratorRunnable());
        exceptionThread.start();
    }

    private static class ExceptionGeneratorRunnable implements Runnable {

        @Override
        public void run() {
            Random random = new Random();
            while (true) {
                try {
                    int exceptionCode = random.nextInt(5);
                    String host = getRandomHost(random);
                    switch (exceptionCode) {
                        case 0:
                            generateArithmeticException(host);
                            break;
                        case 1:
                            generateArrayIndexOutOfBoundsException(host);
                            break;
                        case 2:
                            generateClassCastException(host);
                            break;
                        case 3:
                            generateIllegalArgumentException(null);
                            break;
                        case 4:
                            generateIndexOutOfBoundsException(host);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logException(e);
                }
                sleepForRandomMilliseconds();
            }
        }

        private String getRandomHost(@NotNull Random random) {
            int index = random.nextInt(HOSTS.length);
            return HOSTS[index];
        }
    }

    private static void generateArithmeticException(String host) {
        int a = 10;
        int b = 0;
        int result = a / b;
    }

    private static void generateClassCastException(String host) {
        Object obj = new Integer(10);
        String str = (String) obj;
    }

    private static void generateIllegalArgumentException(String host) {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Invalid host");
        }
    }

    private static void generateIndexOutOfBoundsException(String host) {
        String str = "Hello";
        char ch = str.charAt(10);
    }

    private static void generateNoSuchElementException(String host) {
        List<String> list = new ArrayList<>();
        String element = list.iterator().next();
    }

    private static void generateArrayIndexOutOfBoundsException(String host) {
        int[] array = new int[5];
        int value = array[10];
    }

    private static void logException(@NotNull Exception e) {
        String fileName = "logfile2.txt";

        try (FileOutputStream fos = new FileOutputStream(fileName, true);
             FileChannel channel = fos.getChannel();
             FileLock lock = channel.lock()) {

            // Write to the file
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos))) {
//                LocalDateTime now = LocalDateTime.now();
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS XXX");
//                String formattedDateTime = now.atOffset(ZoneOffset.ofHoursMinutes(5, 30)).format(formatter);

//                LocalDateTime currentDateTime = LocalDateTime.now(ZoneOffset.UTC);
//                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//                String formattedDateTime = currentDateTime.format(outputFormatter);

                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                String formattedDateTime = formatter.format(calendar.getTime());

                String host = HOSTS[new Random().nextInt(HOSTS.length)];

                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                String stackTrace = stringWriter.toString();

                // Replace line separators with "\\n" and tabs with "\\t"
                stackTrace = stackTrace.replace(System.lineSeparator(), "\\n");
                stackTrace = stackTrace.replace("\t", "\\t");

                writer.print("{\"@timestamp\":\"" + formattedDateTime + "\",\"hostname\":\"" + host + "\",\"exception\":\"" + stackTrace + "\"}");
                writer.println();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void sleepForRandomMilliseconds() {
        try {
            Random random = new Random();
            int milliseconds = random.nextInt(250) + 10; // Sleep for 1-6 seconds
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

