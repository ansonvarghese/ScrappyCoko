package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 7/31/2017.
 */

public class EnableNotification {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    private boolean isNotificationEnabled;

    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        isNotificationEnabled = notificationEnabled;
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
}
