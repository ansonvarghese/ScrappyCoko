package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/21/2017.
 */

public class NearFriends  {
    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    public static int VIEW_TYPE_FEED = 0;
    public static int VIEW_TYPE_ACTIVE = 1;
    public static int VIEW_TYPE_ACTIVE_LIST = 2;

    @SerializedName("nearFriendsData")
    private List<NearFriends.NearFriendsData> data = new ArrayList<>();

    public List<NearFriendsData> getData() {
        return data;
    }

    public void setData(List<NearFriendsData> data) {
        this.data = data;
    }

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

    public class NearFriendsData {
        private String userid;
        private String name;
        private String friendcount;
        private String profilePic;
        private String colorCode;
        private String jId;
        private int rank;
        private int moderator;
        private boolean online;
        private boolean newJoined;
        private String lastActive;
        private String designation;
        private String company;
        private String country;


        public String getjId() {
            return jId;
        }

        public void setjId(String jId) {
            this.jId = jId;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFriendcount() {
            return friendcount;
        }

        public void setFriendcount(String friendcount) {
            this.friendcount = friendcount;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }


        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public String getColorCode() {
            return colorCode;
        }

        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
        }


        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public boolean isNewJoined() {
            return newJoined;
        }

        public void setNewJoined(boolean newJoined) {
            this.newJoined = newJoined;
        }

        public int getModerator() {
            return moderator;
        }

        public void setModerator(int moderator) {
            this.moderator = moderator;
        }

        public String getLastActive() {
            return lastActive;
        }

        public void setLastActive(String lastActive) {
            this.lastActive = lastActive;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
