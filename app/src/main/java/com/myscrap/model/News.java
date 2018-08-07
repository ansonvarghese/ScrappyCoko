package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/19/2017.
 */

public class News  {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    private int isEditor;

    @SerializedName("newsData")
    private List<News.NewsData> data = new ArrayList<>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isErrorStatus() {
        return errorStatus;
    }

    public int getEditor() {
        return isEditor;
    }

    public void setEditor(int editor) {
        this.isEditor = editor;
    }

    public void setErrorStatus(boolean errorStatus) {
        this.errorStatus = errorStatus;
    }

    public List<NewsData> getData() {
        return data;
    }

    public void setData(List<NewsData> data) {
        this.data = data;
    }

    public class NewsData {
        private String postType;

        private String postedUserId;

        private String timeStamp;

        private String status;

        @SerializedName("newsPic")
        private String profilePic;

        private String postedUserName;

        private String postId;

        private String postBy;

        private String newsUrl;

        private String albumId;

        private String heading;

        private String subHeading;

        private String location;

        private String editorName;

        private String publisherImage;

        private boolean isEditShow;

        public String getPostType() {
            return postType;
        }

        public void setPostType(String postType) {
            this.postType = postType;
        }

        public String getPostedUserId() {
            return postedUserId;
        }

        public void setPostedUserId(String postedUserId) {
            this.postedUserId = postedUserId;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public String getPostedUserName() {
            return postedUserName;
        }

        public void setPostedUserName(String postedUserName) {
            this.postedUserName = postedUserName;
        }

        public String getPostId() {
            return postId;
        }

        public void setPostId(String postId) {
            this.postId = postId;
        }

        public String getPostBy() {
            return postBy;
        }

        public void setPostBy(String postBy) {
            this.postBy = postBy;
        }

        public String getNewsUrl() {
            return newsUrl;
        }

        public void setNewsUrl(String newsUrl) {
            this.newsUrl = newsUrl;
        }

        public String getAlbumId() {
            return albumId;
        }

        public void setAlbumId(String albumId) {
            this.albumId = albumId;
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


        public boolean isEditShow() {
            return isEditShow;
        }

        public void setEditShow(boolean editShow) {
            isEditShow = editShow;
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
    }
}
