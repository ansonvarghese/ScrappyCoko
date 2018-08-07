package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/16/2017.
 */

public class Like  {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("LikeData")
    private List<LikeData> data = new ArrayList<>();

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

    public List<LikeData> getData() {
        return data;
    }

    public void setData(List<LikeData> data) {
        this.data = data;
    }

    public class LikeData {
        private boolean isFriend;

        private String name;

        private String userId;

        private String likeTimeStamp;

        private String designation;

        private String userCompany;

        private String country;

        private int points;

        private int rank;

        private int moderator;

        private boolean newJoined;

        private String friendCount;

        private String colorCode;

        private String likeProfilePic;

        private String postId;

        private String friendStatus;

        public boolean isFriend() {
            return isFriend;
        }

        public void setFriend(boolean friend) {
            isFriend = friend;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getLikeTimeStamp() {
            return likeTimeStamp;
        }

        public void setLikeTimeStamp(String likeTimeStamp) {
            this.likeTimeStamp = likeTimeStamp;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public String getFriendCount() {
            return friendCount;
        }

        public void setFriendCount(String friendCount) {
            this.friendCount = friendCount;
        }

        public String getLikeProfilePic() {
            return likeProfilePic;
        }

        public void setLikeProfilePic(String likeProfilePic) {
            this.likeProfilePic = likeProfilePic;
        }

        public String getPostId() {
            return postId;
        }

        public void setPostId(String postId) {
            this.postId = postId;
        }

        public String getFriendStatus() {
            return friendStatus;
        }

        public void setFriendStatus(String friendStatus) {
            this.friendStatus = friendStatus;
        }

        public String getUserCompany() {
            return userCompany;
        }

        public void setUserCompany(String userCompany) {
            this.userCompany = userCompany;
        }

        public String getColorCode() {
            return colorCode;
        }

        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
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

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
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
    }
}
