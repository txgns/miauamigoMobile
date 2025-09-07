package com.miaumigo.app.model;

public class Pet {
    private String name;
    private String breed;
    private String age;
    private String location;
    private int imageResource;

    public Pet(String name, String breed, String age, String location, int imageResource) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.location = location;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getAge() {
        return age;
    }

    public String getLocation() {
        return location;
    }

    public int getImageResource() {
        return imageResource;
    }
}
