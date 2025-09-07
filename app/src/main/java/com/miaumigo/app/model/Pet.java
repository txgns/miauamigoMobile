package com.miaumigo.app.model;

public class Pet {
    private String name;
    private String gender;
    private String age;
    private int imageResource;

    public Pet(String name, String gender, String age, int imageResource) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public int getImageResource() {
        return imageResource;
    }
}
