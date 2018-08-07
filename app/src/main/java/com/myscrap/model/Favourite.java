package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/18/2017.
 */

public class Favourite  {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    private boolean insertFavPostData;

    @SerializedName("favouriteDetailsData")
    private List<Favourite.FavouriteData> data = new ArrayList<>();

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

    public List<FavouriteData> getData() {
        return data;
    }

    public void setData(List<FavouriteData> data) {
        this.data = data;
    }

    public boolean isInsertFavPostData() {
        return insertFavPostData;
    }

    public void setInsertFavPostData(boolean insertFavPostData) {
        this.insertFavPostData = insertFavPostData;
    }

    public class FavouriteData {

        @SerializedName("userid")
        private String userId;
        @SerializedName("FriendStatus")
        private String friendStatus;
        private String name;
        private String firstName;
        private String lastName;
        private String designation;
        private String userCompany;
        private String country;
        private String colorCode;
        private String profilePic;
        private int points;
        private int rank;
        private int moderator;
        private boolean newJoined;
        private boolean isFavourited;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getFriendStatus() {
            return friendStatus;
        }

        public void setFriendStatus(String friendStatus) {
            this.friendStatus = friendStatus;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public String getUserCompany() {
            return userCompany;
        }

        public void setUserCompany(String userCompany) {
            this.userCompany = userCompany;
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

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
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

        public int getModerator() {
            return moderator;
        }

        public void setModerator(int moderator) {
            this.moderator = moderator;
        }

        public boolean isFavourited() {
            return isFavourited;
        }

        public void setFavourited(boolean favourited) {
            isFavourited = favourited;
        }
    }
}
