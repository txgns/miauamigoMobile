package com.miaumigo.app.models;

public class ChatMessage {
    private final String message;
    private final boolean isUser;
    private final String time;

    public ChatMessage(String message, boolean isUser, String time) {
        this.message = message;
        this.isUser = isUser;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getTime() {
        return time;
    }
}

