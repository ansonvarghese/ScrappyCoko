package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/19/2017.
 */

public class UserFriendProfile {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    private List<UserFriendProfile.UserFriendProfileData> userProfileData;

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

    public List<UserFriendProfileData> getUserProfileData() {
        return userProfileData;
    }

    public void setUserProfileData(List<UserFriendProfileData> userProfileData) {
        this.userProfileData = userProfileData;
    }

    @SerializedName("pictureUrl")
    private List<PictureUrl> pictureUrl;

    public List<PictureUrl> getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(List<PictureUrl> pictureUrl) {
        this.pictureUrl = pictureUrl;
    }


    public class UserFriendProfileData
    {
        private String userid;
        private String country;
        private String company;
        private String city;
        private String phoneNo;
        private String name;
        private String firstName;
        private String lastName;
        private boolean online;
        private String userLocation;
        private String profilePic;
        private String friendstatus;
        private String friendcount;
        private String postedUserDesignation;
        private String Email;
        private String userCompany;
        private String colorCode;
        private boolean newJoined;
        private String userInterest;
        private String userInterestRoles;
        private String website;
        private String userBio;
        private int points;
        private int moderator;
        private int rank;
        private int profilePercentage;
        private String joinedTime;
        private String jId;


        @SerializedName("companyTag")
        private List<CompanyTagList> tagList;

        @SerializedName("feedsData")
        private List<Feed.FeedItem> data = new ArrayList<>();

        public List<Feed.FeedItem> getData() {
            return data;
        }

        public void setData(List<Feed.FeedItem> data) {
            this.data = data;
        }


        public String getUserid() {
            return userid;
        }



        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getPhoneNo() {
            return phoneNo;
        }

        public void setPhoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
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

        public String getFriendcount() {
            return friendcount;
        }

        public void setFriendcount(String friendcount) {
            this.friendcount = friendcount;
        }

        public String getEmail() {
            return Email;
        }

        public void setEmail(String email) {
            Email = email;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getUserCompany() {
            return userCompany;
        }

        public void setUserCompany(String userCompany) {
            this.userCompany = userCompany;
        }

        public String getUserInterest() {
            return userInterest;
        }

        public void setUserInterest(String userInterest) {
            this.userInterest = userInterest;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getUserBio() {
            return userBio;
        }

        public void setUserBio(String userBio) {
            this.userBio = userBio;
        }

        public String getFriendstatus() {
            return friendstatus;
        }

        public void setFriendstatus(String friendstatus) {
            this.friendstatus = friendstatus;
        }

        public String getPostedUserDesignation() {
            return postedUserDesignation;
        }

        public void setPostedUserDesignation(String postedUserDesignation) {
            this.postedUserDesignation = postedUserDesignation;
        }

        public String getUserLocation() {
            return userLocation;
        }

        public void setUserLocation(String userLocation) {
            this.userLocation = userLocation;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public String getColorCode() {
            return colorCode;
        }

        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public List<CompanyTagList> getTagList() {
            return tagList;
        }

        public void setTagList(List<CompanyTagList> tagList) {
            this.tagList = tagList;
        }

        public String getUserInterestRoles() {
            return userInterestRoles;
        }

        public void setUserInterestRoles(String userInterestRoles) {
            this.userInterestRoles = userInterestRoles;
        }

        public boolean isNewJoined() {
            return newJoined;
        }

        public void setNewJoined(boolean newJoined) {
            this.newJoined = newJoined;
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

        public int getModerator() {
            return moderator;
        }

        public void setModerator(int moderator) {
            this.moderator = moderator;
        }

        public String getJoinedTime() {
            return joinedTime;
        }

        public void setJoinedTime(String joinedTime) {
            this.joinedTime = joinedTime;
        }


        public String getjId()
        {
            return jId;
        }

        public void setjId(String jId) {
            this.jId = jId;
        }

        public int getProfilePercentage() {
            return profilePercentage;
        }

        public void setProfilePercentage(int profilePercentage) {
            this.profilePercentage = profilePercentage;
        }

        public class CompanyTagList {
            private int companyId;
            private String company;

            public int getCompanyId() {
                return companyId;
            }

            public void setCompanyId(int companyId) {
                this.companyId = companyId;
            }

            public String getCompany() {
                return company;
            }

            public void setCompany(String company) {
                this.company = company;
            }
        }
    }
}
