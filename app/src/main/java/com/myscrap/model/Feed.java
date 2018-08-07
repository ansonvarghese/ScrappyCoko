package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/16/2017.
 */

public class Feed  {

    @SerializedName("status")
    private String status;

    @SerializedName("error")
    private boolean errorStatus;



    @SerializedName("feedsData")
    private List<FeedItem> data = new ArrayList<>();

    public List<FeedItem> getData() {
        return data;
    }

    public void setData(List<FeedItem> data) {
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



    public class FeedItem {

        private String postedUserId;

        private String postType;

        private String timeStamp;

        private int likeCount;

        private String status;

        private String postedUserDesignation;

        private String userCompany;

        private int commentCount;

        private int moderator;

        private String postId;

        private String postedUserName;

        private boolean likeStatus;

        private boolean isPostFavourited;

        private String postBy;

        private String profilePic;

        private String postedFriendId;

        private String postedFriendName;

        private String postedFriendProfilePic;

        private int points;

        private int rank;

        private boolean newJoined;

        private String colorCode;

        private boolean isReported;

        private String reportedUserId;

        private String reportId;

        private String reportBy;

        private String heading;

        private String subHeading;

        private String newsLocation;

        private String publisherUrl;

        private String companyName;

        private String companyId;

        private String companyImage;

        private String pageName;

        private String joinedTime;

        private String jid;

        private String eventId;
        private String eventPostedId;
        private String eventName;
        private String eventPicture;
        private boolean isInterested ;
        private String eventDetail;
        private String startDate;
        private String endDate;
        private String startTime;
        private String endTime;

        @SerializedName("albumid")
        private String albumId;

        @SerializedName("pictureUrl")
        private List<PictureUrl> pictureUrl;

        @SerializedName("tagList")
        private List<TagList> tagList;


        public String getJid() {
            return jid;
        }

        public void setJid(String jid) {
            this.jid = jid;
        }

        public String getPostedUserId ()
        {
            return postedUserId;
        }

        public void setPostedUserId (String postedUserId)
        {
            this.postedUserId = postedUserId;
        }

        public String getPostType ()
        {
            return postType;
        }

        public void setPostType (String postType)
        {
            this.postType = postType;
        }

        public String getTimeStamp ()
        {
            return timeStamp;
        }

        public void setTimeStamp (String timeStamp)
        {
            this.timeStamp = timeStamp;
        }

        public int getLikeCount ()
        {
            return likeCount;
        }

        public void setLikeCount (int likeCount)
        {
            this.likeCount = likeCount;
        }

        public String getStatus ()
        {
            return status;
        }

        public void setStatus (String status)
        {
            this.status = status;
        }

        public String getPostedUserDesignation ()
        {
            return postedUserDesignation;
        }

        public void setPostedUserDesignation (String postedUserDesignation)
        {
            this.postedUserDesignation = postedUserDesignation;
        }

        public int getCommentCount ()
        {
            return commentCount;
        }

        public void setCommentCount (int commentCount)
        {
            this.commentCount = commentCount;
        }

        public String getPostId ()
        {
            return postId;
        }

        public void setPostId (String postId)
        {
            this.postId = postId;
        }

        public String getPostedUserName ()
        {
            return postedUserName;
        }

        public void setPostedUserName (String postedUserName)
        {
            this.postedUserName = postedUserName;
        }


        public String getPostBy ()
        {
            return postBy;
        }

        public void setPostBy (String postBy)
        {
            this.postBy = postBy;
        }

        public String getProfilePic ()
        {
            return profilePic;
        }

        public void setProfilePic (String profilePic)
        {
            this.profilePic = profilePic;
        }

        public String getAlbumId()
        {
            return albumId;
        }

        public void setAlbumId(String albumId)
        {
            this.albumId = albumId;
        }

        public List<PictureUrl> getPictureUrl() {
            return pictureUrl;
        }

        public void setPictureUrl(List<PictureUrl> pictureUrl) {
            this.pictureUrl = pictureUrl;
        }

        public boolean isLikeStatus() {
            return likeStatus;
        }

        public void setLikeStatus(boolean likeStatus) {
            this.likeStatus = likeStatus;
        }

        public String getUserCompany() {
            return userCompany;
        }

        public void setUserCompany(String userCompany) {
            this.userCompany = userCompany;
        }

        public String getPostedFriendId() {
            return postedFriendId;
        }

        public void setPostedFriendId(String postedFriendId) {
            this.postedFriendId = postedFriendId;
        }

        public String getPostedFriendName() {
            return postedFriendName;
        }

        public void setPostedFriendName(String postedFriendName) {
            this.postedFriendName = postedFriendName;
        }

        public String getPostedFriendProfilePic() {
            return postedFriendProfilePic;
        }

        public void setPostedFriendProfilePic(String postedFriendProfilePic) {
            this.postedFriendProfilePic = postedFriendProfilePic;
        }

        public boolean isPostFavourited() {
            return isPostFavourited;
        }

        public void setPostFavourited(boolean postFavourited) {
            isPostFavourited = postFavourited;
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

        public boolean isReported() {
            return isReported;
        }

        public void setReported(boolean reported) {
            isReported = reported;
        }

        public String getColorCode() {
            return colorCode;
        }

        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
        }

        public List<TagList> getTagList() {
            return tagList;
        }

        public void setTagList(List<TagList> tagList) {
            this.tagList = tagList;
        }

        public String getReportedUserId() {
            return reportedUserId;
        }

        public void setReportedUserId(String reportedUserId) {
            this.reportedUserId = reportedUserId;
        }

        public String getPageName() {
            return pageName;
        }

        public void setPageName(String pageName) {
            this.pageName = pageName;
        }

        public String getReportId() {
            return reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }

        public boolean isNewJoined() {
            return newJoined;
        }

        public void setNewJoined(boolean newJoined) {
            this.newJoined = newJoined;
        }

        public String getHeading() {
            return heading;
        }

        public void setHeading(String heading) {
            this.heading = heading;
        }

        public String getSubHeading() {
            return subHeading;
        }

        public void setSubHeading(String subHeading) {
            this.subHeading = subHeading;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public String getCompanyImage() {
            return companyImage;
        }

        public void setCompanyImage(String companyImage) {
            this.companyImage = companyImage;
        }

        public int getModerator() {
            return moderator;
        }

        public void setModerator(int moderator) {
            this.moderator = moderator;
        }

        public String getNewsLocation() {
            return newsLocation;
        }

        public void setNewsLocation(String newsLocation) {
            this.newsLocation = newsLocation;
        }

        public String getPublisherUrl() {
            return publisherUrl;
        }

        public void setPublisherUrl(String publisherUrl) {
            this.publisherUrl = publisherUrl;
        }

        public String getReportBy() {
            return reportBy;
        }

        public void setReportBy(String reportBy) {
            this.reportBy = reportBy;
        }

        public String getJoinedTime() {
            return joinedTime;
        }

        public void setJoinedTime(String joinedTime) {
            this.joinedTime = joinedTime;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public String getEventPostedId() {
            return eventPostedId;
        }

        public void setEventPostedId(String eventPostedId) {
            this.eventPostedId = eventPostedId;
        }

        public String getEventPicture() {
            return eventPicture;
        }

        public void setEventPicture(String eventPicture) {
            this.eventPicture = eventPicture;
        }

        public boolean isInterested() {
            return isInterested;
        }

        public void setInterested(boolean interested) {
            isInterested = interested;
        }

        public String getEventDetail() {
            return eventDetail;
        }

        public void setEventDetail(String eventDetail) {
            this.eventDetail = eventDetail;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public class TagList {
            private String taggedId;
            private String taggedUserName;

            public String getTaggedId() {
                return taggedId;
            }

            public void setTaggedId(String taggedId) {
                this.taggedId = taggedId;
            }

            public String getTaggedUserName() {
                return taggedUserName;
            }

            public void setTaggedUserName(String taggedUserName) {
                this.taggedUserName = taggedUserName;
            }
        }
    }
}
