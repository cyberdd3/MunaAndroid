package com.akraft.muna.models.wrappers;

import com.google.gson.annotations.SerializedName;

public class UserId {
    @SerializedName("user_id")
    private long userId;

    public UserId(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
