package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 11/18/2017.
 */

public class EventReport {
    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("eventReported")
    private boolean eventReported;

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

    public boolean isEventReported() {
        return eventReported;
    }

    public void setEventReported(boolean eventReported) {
        this.eventReported = eventReported;
    }
}
