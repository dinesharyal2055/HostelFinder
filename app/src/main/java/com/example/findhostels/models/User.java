package com.example.findhostels.models;

public class User {
    private String fullName;
    private String email;
    private String contact;
    private String password;
    private String image;
    private String type;




    public User(String fullName, String contact, String type) {
        this.fullName = fullName;
        this.contact = contact;
        this.type = type;
    }

    public User(String image) {
        this.image = image;
    }

    public User(String fullName, String contact) {
        this.fullName = fullName;
        this.contact = contact;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
