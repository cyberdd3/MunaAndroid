package com.akraft.muna.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Profile {
    private long id;
    private User user;
    private String facebook;
    private ArrayList<User> team;
    private String avatar;
    private String country;
    private int exp;
    @SerializedName("exp_next")
    private int expNext;
    @SerializedName("exp_curr")
    private int expCurr;
    private int level;

    @SerializedName("incoming_requests")
    private ArrayList<User> incomingRequests;

    //private ArrayList<Quest> quests;
    private ArrayList<Mark> marks;

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getExpNext() {
        return expNext;
    }

    public void setExpNext(int expNext) {
        this.expNext = expNext;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public ArrayList<User> getTeam() {
        return team;
    }

    public void setTeam(ArrayList<User> team) {
        this.team = team;
    }

    public ArrayList<User> getIncomingRequests() {
        return incomingRequests;
    }

    public void setIncomingRequests(ArrayList<User> incomingRequests) {
        this.incomingRequests = incomingRequests;
    }

    public ArrayList<Mark> getMarks() {
        return marks;
    }

    public void setMarks(ArrayList<Mark> marks) {
        this.marks = marks;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExpCurr() {
        return expCurr;
    }

    public void setExpCurr(int expCurr) {
        this.expCurr = expCurr;
    }
}
