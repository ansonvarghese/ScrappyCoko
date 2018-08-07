package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 4/29/2017.
 */

public class ShakeFriend {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("shakeFriendsData")
    private List<ShakeFriend.ShakeFriendData> data = new ArrayList<>();

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

    public List<ShakeFriendData> getData() {
        return data;
    }

    public void setData(List<ShakeFriendData> data) {
        this.data = data;
    }


    public class ShakeFriendData {

        @SerializedName("name")
        private String userName;

        @SerializedName("userid")
        private String userId;

        @SerializedName("designation")
        private String userDesignation;

        @SerializedName("profilePic")
        private String userProfile;

        @SerializedName("distance")
        private String userDistance;

        @SerializedName("unit")
        private String distanceUnit;

        @SerializedName("friendcount")
        private String friendCount;

        private boolean online;



        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserDesignation() {
            return userDesignation;
        }

        public void setUserDesignation(String userDesignation) {
            this.userDesignation = userDesignation;
        }

        public String getUserProfile() {
            return userProfile;
        }

        public void setUserProfile(String userProfile) {
            this.userProfile = userProfile;
        }

        public String getUserDistance() {
            return userDistance;
        }

        public void setUserDistance(String userDistance) {
            this.userDistance = userDistance;
        }

        public String getFriendCount() {
            return friendCount;
        }

        public void setFriendCount(String friendCount) {
            this.friendCount = friendCount;
        }

        public String getDistanceUnit() {
            return distanceUnit;
        }

        public void setDistanceUnit(String distanceUnit) {
            this.distanceUnit = distanceUnit;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }
    }
}
