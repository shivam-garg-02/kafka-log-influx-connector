package com.kafkaloginfluxconnector.counter;

public class MessageCounter {
    private static int messagesLogged;
    private static int messagesRead;
    private static int messagesCorrect;

    static {
        messagesLogged = 0;
        messagesRead = 0;
        messagesCorrect = 0;
    }

    public static int getMessagesLogged() {
        return messagesLogged;
    }

    public static synchronized void incrementMessagesLogged() {
        messagesLogged++;
    }

    public static int getMessagesRead() {
        return messagesRead;
    }

    public static synchronized void incrementMessagesRead() {
        messagesRead++;
    }

    public static int getMessagesCorrect() {
        return messagesCorrect;
    }

    public static synchronized void incrementMessagesCorrect() {
        messagesCorrect++;
    }
}
