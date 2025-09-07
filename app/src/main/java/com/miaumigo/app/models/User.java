package com.miaumigo.app.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String phone;

    // Required empty constructor for Firebase
    public User() {}

    public User(String uid, String name, String email, String phone) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
