package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 1/14/2018.
 */

public class Count {
    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    private int viewersCount;
    private int bumpedCount;
    private int messageCount;
    private int moderatorCount;
    private int notificationCount;
    private int profilePercentage;

    public int getViewersCount() {
        return viewersCount;
    }

    public void setViewersCount(int viewersCount) {
        this.viewersCount = viewersCount;
    }

    public int getBumpedCount() {
        return bumpedCount;
    }

    public void setBumpedCount(int bumpedCount) {
        this.bumpedCount = bumpedCount;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
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

    public int getProfilePercentage() {
        return profilePercentage;
    }

    public void setProfilePercentage(int profilePercentage) {
        this.profilePercentage = profilePercentage;
    }

    public int getModeratorCount() {
        return moderatorCount;
    }

    public void setModeratorCount(int moderatorCount) {
        this.moderatorCount = moderatorCount;
    }
}
