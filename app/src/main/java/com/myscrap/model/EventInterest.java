package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 11/18/2017.
 */

public class EventInterest {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    private boolean isInterested ;

    @SerializedName("eventInterest")
    private int eventInterest;

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

    public int getEventInterest() {
        return eventInterest;
    }

    public void setEventInterest(int eventInterest) {
        this.eventInterest = eventInterest;
    }

    public boolean isInterested() {
        return isInterested;
    }

    public void setInterested(boolean interested) {
        isInterested = interested;
    }
}
