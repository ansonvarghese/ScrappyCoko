package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 11/18/2017.
 */

public class EventGoing {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    private boolean isGoing ;

    @SerializedName("eventGoing")
    private int eventGoing;

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
        return eventGoing;
    }

    public void setEventInterest(int eventGoing) {
        this.eventGoing = eventGoing;
    }

    public boolean isGoing() {
        return isGoing;
    }

    public void setGoing(boolean going) {
        isGoing = going;
    }
}
