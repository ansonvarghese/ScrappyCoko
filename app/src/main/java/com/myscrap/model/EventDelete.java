package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 11/18/2017.
 */

public class EventDelete {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("eventDeleted")
    private boolean eventDeleted;

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

    public boolean isEventDeleted() {
        return eventDeleted;
    }

    public void setEventDeleted(boolean eventDeleted) {
        this.eventDeleted = eventDeleted;
    }
}
