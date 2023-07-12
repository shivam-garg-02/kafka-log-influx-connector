package com.kafkaloginfluxconnector.counter;

public class CounterThread extends Thread{
    @Override
    public void run(){
        while(true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Messages Received: " + MessageCounter.getMessagesRead() + ", Correct Messages: " + MessageCounter.getMessagesCorrect() + ", Messages Logged: " + MessageCounter.getMessagesLogged());
        }
    }
}
