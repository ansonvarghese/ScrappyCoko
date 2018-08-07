package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ms3 on 5/15/2017.
 */

public class PictureUrl implements Serializable
{
    @SerializedName("postid")
    private String postId;

    private String userId;

    private int likeCount;

    private String images;

    private int commentCount;

    private boolean likeStatus;

    private String status;

    @SerializedName("tagList")
    private List<Feed.FeedItem.TagList> tagList;

    private String reportId;

    private String reportedUserId;

    private boolean isReported;

    private String timeStamp;

    public String getPostid ()
    {
        return postId;
    }

    public void setpostId (String postId)
    {
        this.postId = postId;
    }

    public String getImages ()
    {
        return images;
    }

    public void setImages (String images)
    {
        this.images = images;
    }

    public boolean isLikeStatus() {
        return likeStatus;
    }

    public void setLikeStatus(boolean likeStatus) {
        this.likeStatus = likeStatus;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public List<Feed.FeedItem.TagList> getTagList() {
        return tagList;
    }

    public void setTagList(List<Feed.FeedItem.TagList> tagList) {
        this.tagList = tagList;
    }
}
