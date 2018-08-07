package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 6/7/2017.
 */

public class SinglePostDetails  {
    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

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

    @SerializedName("feedsData")
    private List<Feed.FeedItem> data = new ArrayList<>();

    public List<Feed.FeedItem> getData() {
        return data;
    }

    public void setData(List<Feed.FeedItem> data) {
        this.data = data;
    }

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
}
