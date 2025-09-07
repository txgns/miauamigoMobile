package com.miaumigo.app.models;

import java.util.List;

public class Order {
    private String id;
    private String userId;
    private List<CartItem> items;
    private double totalAmount;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
    private long orderDate;
    private long deliveryDate;
    private String notes;

    public Order() {
        // Default constructor required for Firebase
    }

    public Order(String id, String userId, List<CartItem> items, double totalAmount) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = "pending";
        this.orderDate = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public long getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(long orderDate) {
        this.orderDate = orderDate;
    }

    public long getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(long deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFormattedTotalAmount() {
        return String.format("R$ %.2f", totalAmount);
    }

    public int getTotalItems() {
        if (items == null) return 0;
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public String getStatusDisplayName() {
        switch (status.toLowerCase()) {
            case "pending":
                return "Pendente";
            case "processing":
                return "Processando";
            case "shipped":
                return "Enviado";
            case "delivered":
                return "Entregue";
            case "cancelled":
                return "Cancelado";
            default:
                return status;
        }
    }
}

