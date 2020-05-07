package com.example.tagcoffee;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NfcTag {

    private String tagName;
    private String points;
    private String dateTime;
    private String coffeeCategory;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NfcTag() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        this.dateTime = dtf.format(now);
    }

    @Override
    public String toString() {
        return id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getCoffeeCategory() {
        return coffeeCategory;
    }

    public void setCoffeeCategory(String coffeeCategory) {
        this.coffeeCategory = coffeeCategory;
    }
}
