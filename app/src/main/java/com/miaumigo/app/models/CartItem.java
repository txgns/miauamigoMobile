package com.miaumigo.app.models;

public class CartItem {
    private String productId;
    private String productName;
    private String productImage;
    private double price;
    private int quantity;
    private long addedAt;

    public CartItem() {
        // Default constructor required for Firebase
    }

    public CartItem(String productId, String productName, String productImage, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.price = price;
        this.quantity = quantity;
        this.addedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    public double getTotalPrice() {
        return price * quantity;
    }

    public String getFormattedPrice() {
        return String.format("R$ %.2f", price);
    }

    public String getFormattedTotalPrice() {
        return String.format("R$ %.2f", getTotalPrice());
    }
}

