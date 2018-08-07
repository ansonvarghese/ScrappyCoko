package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 5/13/2017.
 */

public class DeviceModel {

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
}
