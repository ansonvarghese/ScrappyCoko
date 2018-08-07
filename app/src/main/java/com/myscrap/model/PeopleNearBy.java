package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/17/2017.
 */

public class PeopleNearBy  {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("peopleNearByData")
    private List<PeopleNearBy.PeopleNearByData> data = new ArrayList<>();



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

    public List<PeopleNearByData> getData() {
        return data;
    }

    public void setData(List<PeopleNearByData> data) {
        this.data = data;
    }

    public class PeopleNearByData {
        private String unit;

        private String distance;

        private String colorCode;

        private String name;

        private String profilePic;

        private String designation;

        private String userid;

        private String friendcount;

        private int moderator;

        private boolean newJoined;

        private boolean online;

        private int points;

        private int rank;

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getFriendcount() {
            return friendcount;
        }

        public void setFriendcount(String friendcount) {
            this.friendcount = friendcount;
        }

        public String getColorCode() {
            return colorCode;
        }

        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
        }

        public boolean isNewJoined() {
            return newJoined;
        }

        public void setNewJoined(boolean newJoined) {
            this.newJoined = newJoined;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public int getModerator() {
            return moderator;
        }

        public void setModerator(int moderator) {
            this.moderator = moderator;
        }
    }
}
