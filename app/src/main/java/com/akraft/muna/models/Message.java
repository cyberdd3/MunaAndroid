package com.akraft.muna.models;


import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.Date;

public class Message extends SugarRecord {
    //private long id;
    private String text;
    private long author;
    private long recipient;
    private Date timestamp;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getAuthor() {
        return author;
    }

    public void setAuthor(long author) {
        this.author = author;
    }

    public long getRecipient() {
        return recipient;
    }

    public void setRecipient(long recipient) {
        this.recipient = recipient;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
