package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ms3 on 3/21/2017.
 */

public class ChatRoomResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;


    @SerializedName("chatRoomData")
    private List<ChatRoom> results;

    public List<ChatRoom> getResults() {
        return results;
    }

    public void setResults(List<ChatRoom> results) {
        this.results = results;
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
