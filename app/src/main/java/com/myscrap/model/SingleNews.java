package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/19/2017.
 */

public class SingleNews  {

    @SerializedName("status")
    private String status;

    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("singleNewsData")
    private List<SingleNews.SingleNewsData> data = new ArrayList<>();

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

    public List<SingleNewsData> getData() {
        return data;
    }

    public void setData(List<SingleNewsData> data) {
        this.data = data;
    }

    public class SingleNewsData {
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

        private String heading;

        private String subHeading;

        private String editorName;

        private String editorId;

        private String publisherImage;

        private String publisherUrl;

        private String publishLocation;

        private String publisherMagazine;

        @SerializedName("albumid")
        private String albumId;

        @SerializedName("pictureUrl")
        private List<PictureUrl> pictureUrl;

        @SerializedName("likeData")
        private List<Like.LikeData> likeData = new ArrayList<>();

        public List<Like.LikeData> getLikeData() {
            return likeData;
        }

        public void setLikeData(List<Like.LikeData> likeData) {
            this.likeData = likeData;
        }


        @SerializedName("commentData")
        private List<Comment.CommentData> commentData = new ArrayList<>();

        public List<Comment.CommentData> getCommentData() {
            return commentData;
        }

        public void setCommentData(List<Comment.CommentData> commentData) {
            this.commentData = commentData;
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

        public String getEditorName() {
            return editorName;
        }

        public void setEditorName(String editorName) {
            this.editorName = editorName;
        }

        public String getPublisherImage() {
            return publisherImage;
        }

        public void setPublisherImage(String publisherImage) {
            this.publisherImage = publisherImage;
        }

        public String getPublisherUrl() {
            return publisherUrl;
        }

        public void setPublisherUrl(String publisherUrl) {
            this.publisherUrl = publisherUrl;
        }

        public String getPublishLocation() {
            return publishLocation;
        }

        public void setPublishLocation(String publishLocation) {
            this.publishLocation = publishLocation;
        }

        public String getPublisherMagazine() {
            return publisherMagazine;
        }

        public void setPublisherMagazine(String publisherMagazine) {
            this.publisherMagazine = publisherMagazine;
        }

        public String getEditorId() {
            return editorId;
        }

        public void setEditorId(String editorId) {
            this.editorId = editorId;
        }
    }
}
