package com.example.lenovo.gupshup.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Lenovo on 13-Aug-16.
 */
public class ChatMessages implements Serializable {
    int messageId;
    String message;
    String time;
    String sender;

    public ChatMessages(int messageId, String message, String time, String sender) {
        this.messageId = messageId;
        this.message = message;
        this.time = time;
        this.sender = sender;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public String getSender() {
        return sender;
    }
}
