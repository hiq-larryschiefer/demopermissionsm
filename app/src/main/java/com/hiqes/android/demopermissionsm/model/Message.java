package com.hiqes.android.demopermissionsm.model;

public class Message {
    private String              message;

    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }
}
