package com.miaumigo.app.models;

public class Message {
    private String id;
    private String chatId;
    private String senderId;
    private String receiverId;
    private String content;
    private String attachmentUrl;
    private String attachmentType; // "image", "pdf", etc.
    private long timestamp;
    private boolean isRead;
    private MessageType type; // TEXT, IMAGE, PDF

    public enum MessageType {
        TEXT, IMAGE, PDF
    }

    public Message() {
        // Required empty constructor for Firebase
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.type = MessageType.TEXT;
    }

    public Message(String id, String chatId, String senderId, String receiverId, String content) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.type = MessageType.TEXT;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}

