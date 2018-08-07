package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ms3 on 12/14/2017.
 */

public class ActiveFriends{
    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;


    @SerializedName("activeFriendsData")
    private List<NearFriends.NearFriendsData> activeFriendsData;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(boolean errorStatus) {
        this.errorStatus = errorStatus;
    }

    public List<NearFriends.NearFriendsData> getActiveFriendsData() {
        return activeFriendsData;
    }

    public void setActiveFriendsData(List<NearFriends.NearFriendsData> activeFriendsData) {
        this.activeFriendsData = activeFriendsData;
    }
}
