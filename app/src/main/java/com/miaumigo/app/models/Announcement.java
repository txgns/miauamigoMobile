package com.miaumigo.app.models;

public class Announcement {
    private String id;
    private String vendorId;
    private String vendorName;
    private String vendorAvatar;
    private String productName;
    private String description;
    private String condition; // "new", "used", "refurbished"
    private double suggestedPrice;
    private AnnouncementType type; // SALE, TRADE, REQUEST
    private String imageUrl;
    private boolean inStock;
    private String category;
    private AnnouncementStatus status; // SOLD, RESERVED, AVAILABLE
    private long createdAt;
    private long updatedAt;

    public enum AnnouncementType {
        SALE, TRADE, REQUEST
    }

    public enum AnnouncementStatus {
        AVAILABLE, RESERVED, SOLD
    }

    public Announcement() {
        // Required empty constructor for Firebase
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.inStock = true;
        this.type = AnnouncementType.SALE;
        this.status = AnnouncementStatus.AVAILABLE;
    }

    public Announcement(String id, String vendorId, String productName, String description, AnnouncementType type) {
        this.id = id;
        this.vendorId = vendorId;
        this.productName = productName;
        this.description = description;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.inStock = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorAvatar() {
        return vendorAvatar;
    }

    public void setVendorAvatar(String vendorAvatar) {
        this.vendorAvatar = vendorAvatar;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public double getSuggestedPrice() {
        return suggestedPrice;
    }

    public void setSuggestedPrice(double suggestedPrice) {
        this.suggestedPrice = suggestedPrice;
    }

    public AnnouncementType getType() {
        return type;
    }

    public void setType(AnnouncementType type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public AnnouncementStatus getStatus() {
        return status != null ? status : AnnouncementStatus.AVAILABLE;
    }

    public void setStatus(AnnouncementStatus status) {
        this.status = status != null ? status : AnnouncementStatus.AVAILABLE;
    }
}

