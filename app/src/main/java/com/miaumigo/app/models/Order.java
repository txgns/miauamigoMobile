package com.miaumigo.app.models;

import java.util.Date;

public class Order {
    private String id;
    private Date date;
    private String status;
    private double total;
    private int itemCount;

    public Order() {
        // Construtor vazio necessário para Firebase
    }

    public Order(String id, Date date, String status, double total, int itemCount) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.total = total;
        this.itemCount = itemCount;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
