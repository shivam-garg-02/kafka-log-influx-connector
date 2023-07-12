package com.kafkaloginfluxconnector.utils;

import java.util.Arrays;
import java.util.List;

public class StandardFields {
    public static final String TIMESTAMP = "@timestamp";
    public static final List<String> HOSTNAME = Arrays.asList("kubernetes", "pod", "name");
    public static final String EXCEPTION = "exception";
}
