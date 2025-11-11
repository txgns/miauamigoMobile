package com.miaumigo.app.models;

public class Chat {
    private String id;
    private String vendor1Id;
    private String vendor2Id;
    private String vendor1Name;
    private String vendor2Name;
    private String vendor1Avatar;
    private String vendor2Avatar;
    private String lastMessage;
    private long lastMessageTimestamp;
    private boolean vendor1Online;
    private boolean vendor2Online;
    private long createdAt;
    private long updatedAt;

    public Chat() {
        // Required empty constructor for Firebase
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Chat(String id, String vendor1Id, String vendor2Id) {
        this.id = id;
        this.vendor1Id = vendor1Id;
        this.vendor2Id = vendor2Id;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVendor1Id() {
        return vendor1Id;
    }

    public void setVendor1Id(String vendor1Id) {
        this.vendor1Id = vendor1Id;
    }

    public String getVendor2Id() {
        return vendor2Id;
    }

    public void setVendor2Id(String vendor2Id) {
        this.vendor2Id = vendor2Id;
    }

    public String getVendor1Name() {
        return vendor1Name;
    }

    public void setVendor1Name(String vendor1Name) {
        this.vendor1Name = vendor1Name;
    }

    public String getVendor2Name() {
        return vendor2Name;
    }

    public void setVendor2Name(String vendor2Name) {
        this.vendor2Name = vendor2Name;
    }

    public String getVendor1Avatar() {
        return vendor1Avatar;
    }

    public void setVendor1Avatar(String vendor1Avatar) {
        this.vendor1Avatar = vendor1Avatar;
    }

    public String getVendor2Avatar() {
        return vendor2Avatar;
    }

    public void setVendor2Avatar(String vendor2Avatar) {
        this.vendor2Avatar = vendor2Avatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public boolean isVendor1Online() {
        return vendor1Online;
    }

    public void setVendor1Online(boolean vendor1Online) {
        this.vendor1Online = vendor1Online;
    }

    public boolean isVendor2Online() {
        return vendor2Online;
    }

    public void setVendor2Online(boolean vendor2Online) {
        this.vendor2Online = vendor2Online;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public String getOtherVendorId(String currentVendorId) {
        if (currentVendorId != null && vendor1Id != null && vendor1Id.equals(currentVendorId)) {
            return vendor2Id != null ? vendor2Id : "";
        }
        return vendor1Id != null ? vendor1Id : "";
    }

    public String getOtherVendorName(String currentVendorId) {
        if (currentVendorId != null && vendor1Id != null && vendor1Id.equals(currentVendorId)) {
            return vendor2Name != null ? vendor2Name : "Vendedor";
        }
        return vendor1Name != null ? vendor1Name : "Vendedor";
    }

    public String getOtherVendorAvatar(String currentVendorId) {
        if (currentVendorId != null && vendor1Id != null && vendor1Id.equals(currentVendorId)) {
            return vendor2Avatar != null ? vendor2Avatar : "";
        }
        return vendor1Avatar != null ? vendor1Avatar : "";
    }

    public boolean isOtherVendorOnline(String currentVendorId) {
        if (currentVendorId != null && vendor1Id != null && vendor1Id.equals(currentVendorId)) {
            return vendor2Online;
        }
        return vendor1Online;
    }
}

