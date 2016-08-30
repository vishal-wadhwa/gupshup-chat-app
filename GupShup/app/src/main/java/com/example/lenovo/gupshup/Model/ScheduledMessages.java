package com.example.lenovo.gupshup.Model;

/**
 * Created by Lenovo on 20-Aug-16.
 */
public class ScheduledMessages {
    private String receiverName;
    private String receiver;
    private String message;
    private String timeStamp;

    public ScheduledMessages(String receiverName, String receiver, String message, String timeStamp) {
        this.receiverName = receiverName;
        this.receiver = receiver;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
