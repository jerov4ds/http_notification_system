package com.jeremiah.model;

public class MessageResponse {

    private Object message;

    public MessageResponse(Object message) {
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
