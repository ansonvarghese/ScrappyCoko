package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 6/5/2017.
 */

public class Online  {

    @SerializedName("status")
    private String status;

    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("Online")
    private String onlineStatus;

    @SerializedName("userOnlineStatusdata")
    private OnlineData onlineData;


    public boolean isErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(boolean errorStatus) {
        this.errorStatus = errorStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OnlineData getOnlineData() {
        return onlineData;
    }

    public void setOnlineData(OnlineData onlineData) {
        this.onlineData = onlineData;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }


    public class OnlineData {

        private boolean online;

        private String timeStamp;

        private String userDesignation;

        private String userCompany;

        private String userCountry;

        private String companyId;



        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }


        public String getUserDesignation() {
            return userDesignation;
        }

        public void setUserDesignation(String userDesignation) {
            this.userDesignation = userDesignation;
        }

        public String getUserCompany() {
            return userCompany;
        }

        public void setUserCompany(String userCompany) {
            this.userCompany = userCompany;
        }

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public String getUserCountry() {
            return userCountry;
        }

        public void setUserCountry(String userCountry) {
            this.userCountry = userCountry;
        }
    }
}
