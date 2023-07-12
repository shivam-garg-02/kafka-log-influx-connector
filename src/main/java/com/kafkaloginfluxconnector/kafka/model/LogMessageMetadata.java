package com.kafkaloginfluxconnector.kafka.model;

import java.util.List;

public class LogMessageMetadata {
    private Long timestamp;
    private String hostname;
    private List<ExceptionInfo> exception;
    private List<String> regexMatches;

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
        return;
    }

    public Long getTimestamp(){
        return timestamp;
    }

    public void setHostname(String hostname){
        this.hostname = hostname;
        return;
    }

    public String getHostname(){
        return hostname;
    }

    public void setException(List<ExceptionInfo> exception){
        this.exception = exception;
        return;
    }

    public List<ExceptionInfo> getException(){
        return exception;
    }

    public void setRegexMatches(List<String> regexMatches) {
        this.regexMatches = regexMatches;
        return;
    }

    public List<String> getRegexMatches() {
        return regexMatches;
    }

    public static class ExceptionInfo {
        private String exceptionType;
        private String callingMethod;
        private String callingClass;
        private String uniqueUID;

        public String getExceptionType() {
            return exceptionType;
        }

        public void setExceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
        }

        public String getCallingMethod() {
            return callingMethod;
        }

        public void setCallingMethod(String callingMethod) {
            this.callingMethod = callingMethod;
        }

        public String getCallingClass() {
            return callingClass;
        }

        public void setCallingClass(String callingClass) {
            this.callingClass = callingClass;
        }

        public String getUniqueUID() {
            return uniqueUID;
        }

        public void setUniqueUID(String uniqueUID) {
            this.uniqueUID = uniqueUID;
        }
    }
}
