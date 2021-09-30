package com.example.findhostels.models;

import java.security.Key;

public class Hostel {

    private String name;
    private String rent;
    private String desc;
    private String type;
    private boolean isWifi;
    private double lat;
    private double lng;
    private String imgUrl;
    private String key;
    private String wardenId;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Hostel() {

    }

    public Hostel(String name, String rent, String desc, String type, boolean isWifi, double lat, double lng, String imgUrl, String wardenId) {
        this.name = name;
        this.rent = rent;
        this.desc = desc;
        this.type = type;
        this.isWifi = isWifi;
        this.lat = lat;
        this.lng = lng;
        this.imgUrl = imgUrl;
        this.wardenId=wardenId;
    }

    public String getWardenId() {
        return wardenId;
    }

    public void setWardenId(String wardenId) {
        this.wardenId = wardenId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRent() {
        return rent;
    }

    public void setRent(String rent) {
        this.rent = rent;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isWifi() {
        return isWifi;
    }



    public void setWifi(boolean wifi) {
        isWifi = wifi;
    }
}
