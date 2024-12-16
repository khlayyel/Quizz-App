package com.isamm.weathapp;

public class ReadWriteUserDetails {
    private String fullName;
    private String doB;
    private String gender;
    private String mobile;
    private int lastScore;


    public ReadWriteUserDetails() {
        // Optional: initialize class properties here
    }

    // Add a constructor that takes four string arguments
    public ReadWriteUserDetails(String fullName, String doB, String gender, String mobile) {
        this.fullName = fullName;
        this.doB = doB;
        this.gender = gender;
        this.mobile = mobile;
    }

    // Add a constructor that takes five arguments (four strings and an int)
    public ReadWriteUserDetails(String fullName, String doB, String gender, String mobile, int lastScore) {
        this.fullName = fullName;
        this.doB = doB;
        this.gender = gender;
        this.mobile = mobile;
        this.lastScore = lastScore;
    }

    // Getters and setters for each field
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDoB() {
        return doB;
    }

    public void setDoB(String doB) {
        this.doB = doB;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getLastScore() {
        return lastScore;
    }

    public void setLastScore(int lastScore) {
        this.lastScore = lastScore;
    }
}