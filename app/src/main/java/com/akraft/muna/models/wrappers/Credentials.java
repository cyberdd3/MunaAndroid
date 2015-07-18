package com.akraft.muna.models.wrappers;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Credentials implements Parcelable {
    private String username;
    private String password;
    private String email;

    public Credentials(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeString(this.email);
    }

    private Credentials(Parcel in) {
        this.username = in.readString();
        this.password = in.readString();
        this.email = in.readString();
    }

    public static final Creator<Credentials> CREATOR = new Creator<Credentials>() {
        public Credentials createFromParcel(Parcel source) {
            return new Credentials(source);
        }

        public Credentials[] newArray(int size) {
            return new Credentials[size];
        }
    };
}
