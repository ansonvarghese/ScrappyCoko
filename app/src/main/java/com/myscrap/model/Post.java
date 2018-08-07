package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ms3 on 5/18/2017.
 */

public class Post  {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("feedsData")
    private Post.PostData data;

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

    public PostData getData() {
        return data;
    }

    public void setData(PostData data) {
        this.data = data;
    }

    public class PostData {
        private String postedUserId;

        private String postType;

        private String timeStamp;

        private int likeCount;

        private String status;

        private String postedUserDesignation;

        private int commentCount;

        private String postId;

        private String postedUserName;

        private boolean likeStatus;

        private String postBy;

        private String profilePic;

        private String postedFriendId;

        private String postedFriendName;

        private String postedFriendProfilePic;

        private String heading;

        private String subHeading;

        private String location;

        @SerializedName("albumid")
        private String albumId;

        @SerializedName("pictureUrl")
        private List<PictureUrl> pictureUrl;

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

        public int getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPostedUserDesignation() {
            return postedUserDesignation;
        }

        public void setPostedUserDesignation(String postedUserDesignation) {
            this.postedUserDesignation = postedUserDesignation;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
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

        public boolean isLikeStatus() {
            return likeStatus;
        }

        public void setLikeStatus(boolean likeStatus) {
            this.likeStatus = likeStatus;
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

        public String getAlbumId() {
            return albumId;
        }

        public void setAlbumId(String albumId) {
            this.albumId = albumId;
        }

        public List<PictureUrl> getPictureUrl() {
            return pictureUrl;
        }

        public void setPictureUrl(List<PictureUrl> pictureUrl) {
            this.pictureUrl = pictureUrl;
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

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}
