package com.miaumigo.app.models;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private long createdAt;
    private long updatedAt;
    private String status; // "Processando", "Enviado", "Entregue", "Cancelado"
    private List<OrderItem> items;
    private double subtotal;
    private double shipping;
    private double discount;
    private double total;
    private Address deliveryAddress;
    private String paymentMethod; // "Cartão", "PIX", "Boleto"
    private String vendorId; // ID do vendedor (se aplicável)
    private String vendorName; // Nome do vendedor/loja
    private String notes; // Observações do pedido

    public Order() {
        this.items = new ArrayList<>();
        this.status = "Processando";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Order(String userId, List<OrderItem> items, Address deliveryAddress, String paymentMethod) {
        this.userId = userId;
        this.items = items != null ? items : new ArrayList<>();
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.status = "Processando";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        calculateTotals();
    }

    private void calculateTotals() {
        this.subtotal = 0.0;
        for (OrderItem item : items) {
            this.subtotal += item.getSubtotal();
        }
        this.total = this.subtotal + this.shipping - this.discount;
        this.updatedAt = System.currentTimeMillis();
    }

    public void recalculateTotal() {
        calculateTotals();
    }

    // Getters e Setters
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        calculateTotals();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getShipping() {
        return shipping;
    }

    public void setShipping(double shipping) {
        this.shipping = shipping;
        calculateTotals();
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        calculateTotals();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        int total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }
}
