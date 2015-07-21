package com.akraft.muna.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Mark extends SugarRecord implements Parcelable {

    private Double lat;
    private Double lon;
    private long author;
    private String username;
    private String name;
    private Date added;
    private boolean active = true;
    private String photo;
    private String thumbnail;
    private String codeword;
    private String note;


    @Exclude
    private boolean bookmarked = false;
    @Exclude
    private boolean hidden = false;

    public Mark(double lat, double lon, long author, String username, String name, Date added, boolean active, String photo, String thumbnail, boolean bookmarked, boolean hidden) {
        this.lat = lat;
        this.lon = lon;
        this.author = author;
        this.username = username;
        this.name = name;
        this.added = added;
        this.active = active;
        this.photo = photo;
        this.thumbnail = thumbnail;
        this.bookmarked = bookmarked;
        this.hidden = hidden;
    }

    //TODO show year if old enough
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM 'at' HH:mm", Locale.US);

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public long getAuthor() {
        return author;
    }

    public void setAuthor(long author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Mark && ((Mark) o).getId().longValue() == getId().longValue();
    }

    public Mark() {
    }

    public String getCodeword() {
        return codeword;
    }

    public void setCodeword(String codeword) {
        this.codeword = codeword;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lon);
        dest.writeLong(this.author);
        dest.writeString(this.username);
        dest.writeString(this.name);
        dest.writeLong(added != null ? added.getTime() : -1);
        dest.writeByte(active ? (byte) 1 : (byte) 0);
        dest.writeString(this.photo);
        dest.writeString(this.thumbnail);
        dest.writeString(this.codeword);
        dest.writeString(this.note);
        dest.writeByte(bookmarked ? (byte) 1 : (byte) 0);
        dest.writeByte(hidden ? (byte) 1 : (byte) 0);
        dest.writeLong(getId() != null ? getId() : 0);
    }

    private Mark(Parcel in) {
        this.lat = in.readDouble();
        this.lon = in.readDouble();
        this.author = in.readLong();
        this.username = in.readString();
        this.name = in.readString();
        long tmpAdded = in.readLong();
        this.added = tmpAdded == -1 ? null : new Date(tmpAdded);
        this.active = in.readByte() != 0;
        this.photo = in.readString();
        this.thumbnail = in.readString();
        this.codeword = in.readString();
        this.note = in.readString();
        this.bookmarked = in.readByte() != 0;
        this.hidden = in.readByte() != 0;
        this.setId(in.readLong());
    }

    public static final Creator<Mark> CREATOR = new Creator<Mark>() {
        public Mark createFromParcel(Parcel source) {
            return new Mark(source);
        }

        public Mark[] newArray(int size) {
            return new Mark[size];
        }
    };
}
