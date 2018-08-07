package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/30/2017.
 */

public class CompanyProfile {
    @SerializedName("status")
    private String status;

    @SerializedName("error")
    private boolean errorStatus;

    private CompanyProfile.CompanyData companyData;

    @SerializedName("feedsData")
    private List<Feed.FeedItem> data = new ArrayList<>();

    public List<Feed.FeedItem> getData() {
        return data;
    }

    public void setData(List<Feed.FeedItem> data) {
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

    public CompanyData getCompanyData() {
        return companyData;
    }

    @SerializedName("pictureUrl")
    private List<PictureUrl> pictureUrl;

    public List<PictureUrl> getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(List<PictureUrl> pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public void setCompanyData(CompanyData companyData) {
        this.companyData = companyData;
    }

    public class CompanyData {

        private String companyAddress;

        private int companyEmployees;

        private String companyPhoneNumber;

        private String companyCountryCode;

        private String companyWebsite;

        private String ownerUserId;

        private String companyName;

        private String companyBio;

        private String companyLike;

        private int joinRequest;

        private boolean isJoined;

        private boolean isFavourite;

        private boolean isFollowing;

        private boolean isEmployee;

        private String companyLatitude;

        private String companyLongitude;

        private String companyCountry;

        private String workingHoursTitle;

        private int joinedStatus;

        private int viewerCount;

        private int isPartner;

        private String companyImage;

        private String companyType;

        private String companyInterests;

        private String companyAffiliations;

        private String userInterestRoles;

        private String companyId;

        @SerializedName("pictureUrl")
        private List<PictureUrl> pictureUrl;


        private List<CompanyData.WorkingHours> workingHours;

        private List<CompanyData.CompanyFeedsData> companyFeedsData;


        public String getCompanyAddress() {
            return companyAddress;
        }

        public void setCompanyAddress(String companyAddress) {
            this.companyAddress = companyAddress;
        }

        public String getOwnerUserId() {
            return ownerUserId;
        }

        public void setOwnerUserId(String ownerUserId) {
            this.ownerUserId = ownerUserId;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getCompanyBio() {
            return companyBio;
        }

        public void setCompanyBio(String companyBio) {
            this.companyBio = companyBio;
        }

        public String getCompanyLike() {
            return companyLike;
        }

        public void setCompanyLike(String companyLike) {
            this.companyLike = companyLike;
        }

        public int getJoinRequest() {
            return joinRequest;
        }

        public void setJoinRequest(int joinRequest) {
            this.joinRequest = joinRequest;
        }

        public boolean isJoined() {
            return isJoined;
        }

        public void setJoined(boolean joined) {
            isJoined = joined;
        }

        public boolean isFollowing() {
            return isFollowing;
        }

        public void setFollowing(boolean following) {
            isFollowing = following;
        }

        public String getCompanyLatitude() {
            return companyLatitude;
        }

        public void setCompanyLatitude(String companyLatitude) {
            this.companyLatitude = companyLatitude;
        }

        public String getCompanyCountry() {
            return companyCountry;
        }

        public void setCompanyCountry(String companyCountry) {
            this.companyCountry = companyCountry;
        }

        public String getWorkingHoursTitle() {
            return workingHoursTitle;
        }

        public void setWorkingHoursTitle(String workingHoursTitle) {
            this.workingHoursTitle = workingHoursTitle;
        }

        public int getJoinedStatus() {
            return joinedStatus;
        }

        public void setJoinedStatus(int joinedStatus) {
            this.joinedStatus = joinedStatus;
        }

        public String getCompanyImage() {
            return companyImage;
        }

        public void setCompanyImage(String companyImage) {
            this.companyImage = companyImage;
        }

        public String getCompanyType() {
            return companyType;
        }

        public void setCompanyType(String companyType) {
            this.companyType = companyType;
        }

        public String getCompanyInterests() {
            return companyInterests;
        }

        public void setCompanyInterests(String companyInterests) {
            this.companyInterests = companyInterests;
        }

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public String getCompanyLongitude() {
            return companyLongitude;
        }

        public void setCompanyLongitude(String companyLongitude) {
            this.companyLongitude = companyLongitude;
        }

        public List<CompanyFeedsData> getCompanyFeedsData() {
            return companyFeedsData;
        }

        public void setCompanyFeedsData(List<CompanyFeedsData> companyFeedsData) {
            this.companyFeedsData = companyFeedsData;
        }

        public List<WorkingHours> getWorkingHours() {
            return workingHours;
        }

        public void setWorkingHours(List<WorkingHours> workingHours) {
            this.workingHours = workingHours;
        }

        public String getCompanyPhoneNumber() {
            return companyPhoneNumber;
        }

        public void setCompanyPhoneNumber(String companyPhoneNumber) {
            this.companyPhoneNumber = companyPhoneNumber;
        }

        public String getCompanyWebsite() {
            return companyWebsite;
        }

        public void setCompanyWebsite(String companyWebsite) {
            this.companyWebsite = companyWebsite;
        }

        public int getCompanyEmployees() {
            return companyEmployees;
        }

        public void setCompanyEmployees(int companyEmployees) {
            this.companyEmployees = companyEmployees;
        }

        public List<PictureUrl> getPictureUrl() {
            return pictureUrl;
        }

        public void setPictureUrl(List<PictureUrl> pictureUrl) {
            this.pictureUrl = pictureUrl;
        }

        public boolean isEmployee() {
            return isEmployee;
        }

        public void setEmployee(boolean employee) {
            isEmployee = employee;
        }

        public boolean isFavourite() {
            return isFavourite;
        }

        public void setFavourite(boolean favourite) {
            isFavourite = favourite;
        }

        public String getUserInterestRoles() {
            return userInterestRoles;
        }

        public void setUserInterestRoles(String userInterestRoles) {
            this.userInterestRoles = userInterestRoles;
        }

        public String getCompanyCountryCode() {
            return companyCountryCode;
        }

        public void setCompanyCountryCode(String companyCountryCode) {
            this.companyCountryCode = companyCountryCode;
        }

        public int getViewerCount() {
            return viewerCount;
        }

        public void setViewerCount(int viewerCount) {
            this.viewerCount = viewerCount;
        }

        public int getIsPartner() {
            return isPartner;
        }

        public void setIsPartner(int isPartner) {
            this.isPartner = isPartner;
        }

        public String getCompanyAffiliations() {
            return companyAffiliations;
        }

        public void setCompanyAffiliations(String companyAffiliations) {
            this.companyAffiliations = companyAffiliations;
        }

        private class CompanyFeedsData {

            private String ownerName;

            private String postedUserId;

            private String postType;

            private String timeStamp;

            private String likeCount;

            private String status;

            private String ownerId;

            private String commentCount;

            private String ownerProfilePic;

            private boolean likeStatus;

            private String postId;

            private String postedUserName;

            private String postBy;

            private String profilePic;

            @SerializedName("pictureUrl")
            private List<PictureUrl> pictureUrl;

            public String getOwnerName() {
                return ownerName;
            }

            public void setOwnerName(String ownerName) {
                this.ownerName = ownerName;
            }

            public String getPostedUserId() {
                return postedUserId;
            }

            public void setPostedUserId(String postedUserId) {
                this.postedUserId = postedUserId;
            }

            public String getPostType() {
                return postType;
            }

            public void setPostType(String postType) {
                this.postType = postType;
            }

            public String getTimeStamp() {
                return timeStamp;
            }

            public void setTimeStamp(String timeStamp) {
                this.timeStamp = timeStamp;
            }

            public String getLikeCount() {
                return likeCount;
            }

            public void setLikeCount(String likeCount) {
                this.likeCount = likeCount;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getOwnerId() {
                return ownerId;
            }

            public void setOwnerId(String ownerId) {
                this.ownerId = ownerId;
            }

            public String getCommentCount() {
                return commentCount;
            }

            public void setCommentCount(String commentCount) {
                this.commentCount = commentCount;
            }

            public String getOwnerProfilePic() {
                return ownerProfilePic;
            }

            public void setOwnerProfilePic(String ownerProfilePic) {
                this.ownerProfilePic = ownerProfilePic;
            }

            public boolean isLikeStatus() {
                return likeStatus;
            }

            public void setLikeStatus(boolean likeStatus) {
                this.likeStatus = likeStatus;
            }

            public String getPostId() {
                return postId;
            }

            public void setPostId(String postId) {
                this.postId = postId;
            }

            public String getPostedUserName() {
                return postedUserName;
            }

            public void setPostedUserName(String postedUserName) {
                this.postedUserName = postedUserName;
            }

            public String getPostBy() {
                return postBy;
            }

            public void setPostBy(String postBy) {
                this.postBy = postBy;
            }

            public String getProfilePic() {
                return profilePic;
            }

            public void setProfilePic(String profilePic) {
                this.profilePic = profilePic;
            }

            public List<PictureUrl> getPictureUrl() {
                return pictureUrl;
            }

            public void setPictureUrl(List<PictureUrl> pictureUrl) {
                this.pictureUrl = pictureUrl;
            }
        }

        public class WorkingHours {

            private String closeTime;

            private String openTime;

            private String day;

            public String getCloseTime() {
                return closeTime;
            }

            public void setCloseTime(String closeTime) {
                this.closeTime = closeTime;
            }

            public String getOpenTime() {
                return openTime;
            }

            public void setOpenTime(String openTime) {
                this.openTime = openTime;
            }

            public String getDay() {
                return day;
            }

            public void setDay(String day) {
                this.day = day;
            }
        }
    }
}
