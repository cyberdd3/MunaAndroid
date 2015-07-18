package com.akraft.muna.models;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagesPage {
    private long count;
    private Integer next;
    private ArrayList<Message> results;

    private static Pattern pattern = Pattern.compile("\\?page=([0-9]+)");

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }


    public ArrayList<Message> getResults() {
        return results;
    }

    public void setResults(ArrayList<Message> results) {
        this.results = results;
    }

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }
}
